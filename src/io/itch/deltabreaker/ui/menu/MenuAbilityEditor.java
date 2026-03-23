package io.itch.deltabreaker.ui.menu;

import java.awt.Point;
import java.util.ArrayList;

import io.itch.deltabreaker.core.Inventory;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemAbility;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.ui.UIBox;

public class MenuAbilityEditor extends Menu {

	private ItemProperty item;
	private ArrayList<ItemProperty> abilities = new ArrayList<>();

	private boolean onGrid = true;
	private int gridX = 0, gridY = 0;
	private Vector3f cursorPos;
	private boolean picked = false;
	private boolean pickedFromList = true;
	private int pickPos = -1;
	private int heldRotation = 0;
	private boolean[] heldPiece;

	public MenuAbilityEditor(Vector3f position, ItemProperty item) {
		super(position, new String[] { "" }, 124, 16);
		this.item = item;
		ItemProperty[] items = ItemProperty.searchForType(ItemProperty.TYPE_GEM_ABILITY, Inventory.getItemList().toArray(new ItemProperty[Inventory.getItemList().size()]), false);
		for (ItemProperty i : items) {
			abilities.add(i);
			System.out.println(i.abilities[0]);
		}
		openTo = 90;

		Startup.staticView.targetPosition = new Vector3f(position.getX() / 2 - 1 + width / 4, position.getY() / 2 - openTo / 4, Startup.staticView.position.getZ());
		StateManager.currentState.cursor.staticView = true;
		cursorPos = Vector3f.add(StateManager.currentState.cursor.position, 0, 0, 0);
		StateManager.currentState.cursor.warpLocation(Vector3f.add(position, 0, -9, 0));
	}

	@Override
	public void action(String command, Unit unit) {
		if (subMenu.size() == 0) {
			if (!command.equals("back")) {
				switch (command) {

				case "":
					if (!picked && !onGrid && abilities.size() > 0) {
						picked = true;
						pickPos = selected;
						pickedFromList = true;
						onGrid = true;
						heldRotation = 0;
						heldPiece = ItemAbility.valueOf(abilities.get(pickPos).abilities[0]).size;
						AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
						break;
					}
					if (!picked && onGrid) {
						boolean wasPicked = false;
						for (int i = 0; i < item.abilities.length; i++) {
							boolean[] abilityList = ItemAbility.valueOf(item.abilities[i]).size;
							Point position = item.positions[i];

							// Rotate ability list for matching
							int rotations = item.rotations[i];
							for (int r = 0; r < rotations; r++) {
								abilityList = rotateRight(abilityList);
							}

							int posX = gridX - position.x;
							int posY = gridY - position.y;
							int pos = posX + posY * 5;

							if (pos > -1 && pos < abilityList.length && abilityList[pos]) {
								wasPicked = true;
								picked = true;
								pickPos = i;
								pickedFromList = false;
								heldRotation = rotations;
								heldPiece = abilityList;
								AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
								break;
							}
						}
						if (wasPicked) {
							break;
						}
					}
					if (picked) {
						if (posValid()) {
							if (pickedFromList) {
								String[] abilities = new String[item.abilities.length + 1];
								Point[] positions = new Point[abilities.length];
								int[] rotations = new int[abilities.length];
								for (int i = 0; i < item.abilities.length; i++) {
									abilities[i] = item.abilities[i];
									positions[i] = item.positions[i];
									rotations[i] = item.rotations[i];
								}
								abilities[abilities.length - 1] = ItemAbility.valueOf(this.abilities.get(pickPos).abilities[0]).name();
								positions[positions.length - 1] = new Point(gridX, gridY);
								rotations[rotations.length - 1] = heldRotation;
								item.abilities = abilities;
								item.positions = positions;
								item.rotations = rotations;
								this.abilities.remove(pickPos);
								onGrid = false;
								if (selected > this.abilities.size() - 1) {
									selected = this.abilities.size() - 1;
								}
							} else {
								item.positions[pickPos] = new Point(gridX, gridY);
								item.rotations[pickPos] = heldRotation;
							}
							picked = false;
							pickPos = -1;
							heldRotation = 0;
							AudioManager.getSound("menu_open.ogg").play(AudioManager.defaultMainSFXGain, false);
						} else {
							AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
						}
					}
					break;

				case "shift_right":
					if (picked) {
						heldRotation++;
						if (heldRotation > 3) {
							heldRotation = 0;
						}
						heldPiece = rotateRight(heldPiece);
					}
					break;

				case "shift_left":
					if (picked) {
						heldRotation--;
						if (heldRotation < 0) {
							heldRotation = 3;
						}
						heldPiece = rotateLeft(heldPiece);
					}
					break;

				case "left":
					if (onGrid) {
						if (gridX > 0) {
							gridX--;
							AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
						}
					} else {
						onGrid = true;
						AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
					}
					break;

				case "right":
					if (gridX < item.capacity - 1) {
						gridX++;
						AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
					} else if (onGrid && !picked) {
						onGrid = false;
						AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
					}
					break;

				}
			} else {
				if (picked) {
					picked = false;
					pickPos = -1;
					AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
					if (pickedFromList) {
						onGrid = false;
					}
					return;
				}

				close();
				AudioManager.getSound("menu_close.ogg").play(AudioManager.defaultMainSFXGain, false);
			}
		} else {
			subMenu.get(0).action(command, null);
		}
	}

	private boolean posValid() {
		int height = (int) Math.ceil(heldPiece.length / 5f);

		for (int x = 0; x < Math.min(5, heldPiece.length); x++) {
			for (int y = 0; y < height; y++) {
				int pos = x + y * 5;
				if (pos < heldPiece.length && pos > -1) {
					if (heldPiece[pos]) {
						if (gridX + x > item.capacity - 1) {
							return false;
						}
						if (gridY + y > item.capacity - 1) {
							return false;
						}

						// Loop through placed abilities
						for (int i = 0; i < item.abilities.length; i++) {
							if (!pickedFromList && i == pickPos) {
								continue;
							}
							
							boolean[] abilityList = ItemAbility.valueOf(item.abilities[i]).size;
							Point position = item.positions[i];

							// Rotate ability list for matching
							int rotations = item.rotations[i];
							for (int r = 0; r < rotations; r++) {
								abilityList = rotateRight(abilityList);
							}

							int posXOnBoard = gridX + x;
							int posYOnBoard = gridY + y;

							int posXOnPiece = posXOnBoard - position.x;
							int posYOnPiece = posYOnBoard - position.y;

							int posTwo = posXOnPiece + posYOnPiece * 5;
							if (posTwo < abilityList.length && posTwo > -1 && abilityList[posTwo]) {
								return false;
							}
						}
					}
				}
			}
		}

		return true;
	}

	public void tick() {
		if (options.length == 0) {
			open = false;
		}
		if (open) {
			if (height < openTo) {
				height = Math.min(height + 3, openTo);
			}
			if (height > openTo) {
				height = Math.max(height - 3, openTo);
			}
		} else {
			if (height > 0) {
				height = Math.max(height - 3, 16);
			}
		}
		if (subMenu.size() > 0) {
			subMenu.get(0).tick();
			if (!subMenu.get(0).open && subMenu.get(0).height <= 16) {
				subMenu.remove(0);
			}
		}
		if (selected < 0) {
			selected = 0;
		}

		if (subMenu.size() == 0 && open) {
			if (moveCamera) {
				Startup.staticView.targetPosition = new Vector3f(position.getX() / 2 - 1 + width / 4, position.getY() / 2 - openTo / 4, Startup.staticView.position.getZ());
			}
		}

		if (subMenu.size() == 0 && open) {
			if (!onGrid) {
				StateManager.currentState.cursor.setLocation(new Vector3f(position.getX() + 36, position.getY() - 12 - Math.min(9, selected) * 7 - 6, position.getZ() + 2));
			} else {
				StateManager.currentState.cursor.setLocation(new Vector3f(position.getX() + gridX * 7f - 1, position.getY() - 12 - gridY * 7f - 6, position.getZ() + 2));
			}
		}
	}

	@Override
	public void move(int amt) {
		if (subMenu.size() == 0) {
			if (!onGrid) {
				if (abilities.size() > 1) {
					AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
					selected += amt;
					if (selected < 0) {
						selected = abilities.size() - 1;
					}
					if (selected > abilities.size() - 1) {
						selected = 0;
					}
				}
			} else {
				boolean play = true;
				gridY += amt;
				if (gridY < 0) {
					gridY = 0;
					play = false;
				}
				if (gridY > item.capacity - 1) {
					gridY = item.capacity - 1;
					play = false;
				}
				if (play) {
					AudioManager.getSound("move_cursor.ogg").play(AudioManager.defaultMainSFXGain, false);
				}
			}
		} else {
			subMenu.get(0).move(amt);
		}
	}

	@Override
	public void close() {
		super.close();
		StateManager.currentState.cursor.staticView = false;
		StateManager.currentState.cursor.warpLocation(Vector3f.add(cursorPos, 0, 0, 0));
	}

	public boolean[] rotateRight(boolean[] input) {
		boolean[] output = new boolean[25];

		int pieceHeight = (int) Math.ceil(input.length / 5f);
		int xOffset = 5 - pieceHeight;

		// Rotate array
		for (int x = 0; x < Math.min(5, input.length); x++) {
			for (int y = 0; y < pieceHeight; y++) {
				int pos = x + y * 5;
				if (pos < input.length) {
					if (input[pos]) {
						int outputPos = Math.abs(4 - y) + x * 5 - xOffset;
						output[outputPos] = true;
					}
				}
			}
		}

		// Trim array
		int trim = 0;
		for (int i = output.length - 1; i > 0; i--) {
			if (!output[i]) {
				trim++;
			} else {
				break;
			}
		}
		if (trim > 0) {
			boolean[] trimmedOutput = new boolean[output.length - trim];
			for (int i = 0; i < trimmedOutput.length; i++) {
				trimmedOutput[i] = output[i];
			}
			output = trimmedOutput;
		}

		return output;
	}

	public boolean[] rotateLeft(boolean[] input) {
		boolean[] output = new boolean[25];

		int pieceHeight = (int) Math.ceil(input.length / 5f);

		// Find height offset
		int minLength = 5;
		for (int y = 0; y < pieceHeight; y++) {
			int length = 0;
			boolean found = false;
			for (int x = Math.min(4, input.length - 1); x > 0; x--) {
				int pos = x + y * 5;
				if (pos >= input.length || !input[pos]) {
					length++;
					found = true;
				} else {
					break;
				}
			}
			if (length < minLength && found) {
				minLength = length;
			}
		}

		// Rotate array
		for (int x = 0; x < Math.min(5, input.length); x++) {
			for (int y = 0; y < pieceHeight; y++) {
				int pos = x + y * 5;
				if (pos < input.length) {
					if (input[pos]) {
						int outputPos = y + (4 - x) * 5 - minLength * 5;
						output[outputPos] = true;
					}
				}
			}
		}

		// Trim array
		int trim = 0;
		for (int i = output.length - 1; i > 0; i--) {
			if (!output[i]) {
				trim++;
			} else {
				break;
			}
		}
		if (trim > 0) {
			boolean[] trimmedOutput = new boolean[output.length - trim];
			for (int i = 0; i < trimmedOutput.length; i++) {
				trimmedOutput[i] = output[i];
			}
			output = trimmedOutput;
		}

		return output;
	}

	public void render() {
		UIBox.render(position, width, height);

		item.capacity = 5;
		
		// Draw grid
		int offset = -6;
		for (int x = 0; x < item.capacity; x++) {
			for (int y = 0; y < item.capacity; y++) {
				if (height > 15 + offset + y * 7) {
					BatchSorter.add("menu_cust_outline.dae", "menu_cust_outline.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 8 + x * 7, -9 - y * 7 + offset, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f),
							new Vector4f(1, 1, 1, 1), false, true);
				}
			}
		}

		// Draw hovered ability
		String text = "";
		Vector4f color = Vector4f.COLOR_BASE;
		
		if(onGrid) {
			for (int i = 0; i < item.abilities.length; i++) {
				if(!pickedFromList && i == pickPos) {
					break;
				}
				boolean[] abilityList = ItemAbility.valueOf(item.abilities[i]).size;
				Point position = item.positions[i];

				// Rotate ability list for matching
				int rotations = item.rotations[i];
				for (int r = 0; r < rotations; r++) {
					abilityList = rotateRight(abilityList);
				}

				int posX = gridX - position.x;
				int posY = gridY - position.y;
				int pos = posX + posY * 5;

				if (pos > -1 && pos < abilityList.length && abilityList[pos]) {
					text = ItemAbility.valueOf(item.abilities[i]).toString();
					color = ItemAbility.valueOf(item.abilities[i]).color;
					break;
				}
			}
		}
		
		if (this.height > 15 + 6 * 7) {
			TextRenderer.render(text, Vector3f.add(position, 7, 36.5f - 6 * 7, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), color, true);
		}
		
		// Draw ability list
		for (int i = 0; i < Math.min(10, abilities.size()); i++) {
			if ((picked && pickedFromList) && i == Math.min(9, pickPos)) {
				continue;
			}
			ItemAbility ability = ItemAbility.valueOf(abilities.get(i + Math.max(0, selected - 9)).abilities[0]);
			int height = (int) Math.ceil(ability.size.length / 5f);
			for (int x = 0; x < Math.min(5, ability.size.length); x++) {
				for (int y = 0; y < height; y++) {
					if (this.height > 15 + i * 7 + offset) {
						int pos = x + y * 5;
						if (pos < ability.size.length) {
							if (ability.size[pos]) {
								BatchSorter.add("pixel.dae", "pixel.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 47.5f + x, -9 - i * 7 - y + height / 4f + offset, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f),
										ability.getColor(), false, true);
							}
						}
					}
				}
			}

			if (this.height > 15 + i * 7 + offset) {
				TextRenderer.render(ability.toString(), Vector3f.add(position, 57, -9.5f - i * 7 + offset, 1), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f), ability.getColor(), true);
			}
		}

		// Draw picked ability
		if (picked && pickPos > -1) {
			ItemAbility ability = ItemAbility.valueOf((pickedFromList) ? abilities.get(pickPos).abilities[0] : item.abilities[pickPos]);
			int height = (int) Math.ceil(heldPiece.length / 5f);
			for (int x = 0; x < Math.min(5, heldPiece.length); x++) {
				for (int y = 0; y < height; y++) {
					int pos = x + y * 5;
					if (pos < heldPiece.length) {
						if (heldPiece[pos]) {
							BatchSorter.add("menu_cust_center.dae", "menu_cust_center.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(StateManager.currentState.cursor.position, 9 + x * 7, 3 - y * 7, -1), new Vector3f(0, 0, 0),
									new Vector3f(0.5f, 0.5f, 0.5f), ability.getColor(), false, true);
						}
					}
				}
			}
		}

		// Draw weapon abilities
		for (int i = 0; i < item.abilities.length; i++) {
			if (!pickedFromList && pickPos == i) {
				continue;
			}

			ItemAbility ability = ItemAbility.valueOf(item.abilities[i]);
			Point point = item.positions[i];
			boolean[] drawnPiece = ability.size;
			for (int r = 0; r < item.rotations[i]; r++) {
				drawnPiece = rotateRight(drawnPiece);
			}
			for (int x = 0; x < Math.min(5, drawnPiece.length); x++) {
				for (int y = 0; y < height; y++) {
					if (height > 15 + y * 7) {
						int pos = x + y * 5;
						if (pos < drawnPiece.length) {
							if (drawnPiece[pos]) {
								BatchSorter.add("menu_cust_center.dae", "menu_cust_center.png", "static_3d", Material.DEFAULT.toString(), Vector3f.add(position, 8 + (x + point.x) * 7, -9 - (y + point.y) * 7 + offset, 1), new Vector3f(0, 0, 0),
										new Vector3f(0.5f, 0.5f, 0.5f), ability.getColor(), false, true);
							}
						}
					}
				}
			}
		}
	}

}

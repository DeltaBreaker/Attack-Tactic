package io.itch.deltabreaker.ui;

import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.event.DialogueScript;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.graphics.TextRenderer;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class TextBox extends UIBox {

	public String[][] boxText;
	public String[][] units;
	public String[][] expressions;
	public String[][] mouth;
	public int[] highlights;

	public int currentBlock = 0;

	public int currentLine = 0;
	public int charsRevealed = 0;
	public int charTimer = 0;
	public int[][] charTime;

	public int openInt = 0;
	public int openTo = 32;

	public boolean close = false;

	public int blinkTimer = 0;
	public int blinkTime = 720;
	public int blinkFrameTimer = 0;
	public int blinkFrameTime = 4;
	public int blinkFrame = 0;
	public int blinkFrames = 4;

	public int talkFrame = 0;
	public int talkFrames = 2;
	public int talkFrameTimer = 0;
	public int talkFrameTime = 10;

	public TextBox(String[][] text, int[][] charTime, String[][] units, String[][] expressions, String[][] mouth, int[] highlights) {
		super(new Vector3f(-76, -24, -80), 160, 0);
		this.boxText = text;
		this.charTime = charTime;
		this.units = units;
		this.expressions = expressions;
		this.mouth = mouth;
		this.highlights = highlights;
	}

	public TextBox(DialogueScript script) {
		super(new Vector3f(-76, -24, -80), 160, 0);
		boxText = script.text;
		charTime = script.charTime;
		units = script.units;
		expressions = script.expressions;
		mouth = script.mouth;
		highlights = script.highlights;
	}

	public void tick() {
		Startup.staticView.setPosition(0, 0, 0);
		position.setY(-40 + openInt / 2);
		if (!close) {
			if (openInt < openTo) {
				openInt = Math.min(openTo, openInt + 2);
			} else {
				if (charTimer < charTime[currentBlock][currentLine]) {
					charTimer++;
				} else {
					charTimer = 0;
					if (charsRevealed < boxText[currentBlock][currentLine].length()) {
						charsRevealed++;
						if (boxText[currentBlock][currentLine].charAt(charsRevealed - 1) != ' ') {
							AudioManager.getSound("text.ogg").play(AudioManager.defaultMainSFXGain, false);
						}
					} else {
						if (currentLine < 1 && boxText[currentBlock].length > 1) {
							currentLine++;
							charsRevealed = 0;
						}
					}
				}
			}
			if (blinkTimer < blinkTime) {
				blinkTimer++;
			} else {
				if (blinkFrameTimer < blinkFrameTime) {
					blinkFrameTimer++;
				} else {
					blinkFrameTimer = 0;
					if (blinkFrame < blinkFrames - 1) {
						blinkFrame++;
					} else {
						blinkFrame = 0;
						blinkTimer = 0;
					}
				}
			}
			if (talkFrameTimer < talkFrameTime) {
				talkFrameTimer++;
			} else {
				talkFrameTimer = 0;
				if (talkFrame < talkFrames - 1) {
					talkFrame++;
				} else {
					talkFrame = 0;
				}
			}
		} else {
			if (openInt > 0) {
				openInt = Math.max(0, openInt - 2);
			}
		}
		height = openInt;
	}

	public void render() {
		super.render();
		if (!close) {
			if (openInt == openTo) {
				for (int i = 0; i < units[currentBlock].length; i++) {
					if (highlights[currentBlock] == i) {
						BatchSorter.add(units[currentBlock][i] + ".dae", units[currentBlock][i] + ".png", "static_3d", Material.DEFAULT.toString(),
								Vector3f.add(position, new Vector3f(-position.getX() + i * 40 - (units[currentBlock].length - 1) * 20, 36 - openInt / 2, 0)), (i + 1 <= units[currentBlock].length / 2.0) ? Vector3f.EMPTY : Vector3f.FLIPPED,
								Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, false, true);
						BatchSorter.add(expressions[currentBlock][i] + "_" + blinkFrame + ".dae", expressions[currentBlock][i] + "_" + blinkFrame + ".png", "static_3d", Material.DEFAULT.toString(),
								Vector3f.add(position, new Vector3f(-position.getX() + i * 40 - (expressions[currentBlock].length - 1) * 20, 36 - openInt / 2, 0.05f)),
								(i + 1 <= expressions[currentBlock].length / 2.0) ? Vector3f.EMPTY : Vector3f.FLIPPED, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, false, true);
						if (charsRevealed == boxText[currentBlock][currentLine].length()) {
							BatchSorter.add(mouth[currentBlock][i] + ".dae", mouth[currentBlock][i] + ".png", "static_3d", Material.DEFAULT.toString(),
									Vector3f.add(position, new Vector3f(-position.getX() + i * 40 - (expressions[currentBlock].length - 1) * 20, 36 - openInt / 2, 0.05f)),
									(i + 1 <= expressions[currentBlock].length / 2.0) ? Vector3f.EMPTY : Vector3f.FLIPPED, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, false, true);
						} else {
							BatchSorter.add("portrait_mouth_talk_" + talkFrame + ".dae", "portrait_mouth_talk_" + talkFrame + ".png", "static_3d", Material.DEFAULT.toString(),
									Vector3f.add(position, new Vector3f(-position.getX() + i * 40 - (expressions[currentBlock].length - 1) * 20, 36 - openInt / 2, 0.05f)),
									(i + 1 <= expressions[currentBlock].length / 2.0) ? Vector3f.EMPTY : Vector3f.FLIPPED, Vector3f.SCALE_HALF, Vector4f.COLOR_BASE, false, true);
						}
					} else {
						BatchSorter.add(units[currentBlock][i] + ".dae", units[currentBlock][i] + ".png", "static_3d", Material.DEFAULT.toString(),
								Vector3f.add(position, new Vector3f(-position.getX() + i * 40 - (units[currentBlock].length - 1) * 20, 36 - openInt / 2, 0)), (i + 1 <= units[currentBlock].length / 2.0) ? Vector3f.EMPTY : Vector3f.FLIPPED,
								Vector3f.SCALE_HALF, Vector4f.COLOR_GRAY, false, true);
						BatchSorter.add(expressions[currentBlock][i] + "_0.dae", expressions[currentBlock][i] + "_0.png", "static_3d", Material.DEFAULT.toString(),
								Vector3f.add(position, new Vector3f(-position.getX() + i * 40 - (units[currentBlock].length - 1) * 20, 36 - openInt / 2, 0.05f)),
								(i + 1 <= units[currentBlock].length / 2.0) ? Vector3f.EMPTY : Vector3f.FLIPPED, Vector3f.SCALE_HALF, Vector4f.COLOR_GRAY, false, true);
						BatchSorter.add(mouth[currentBlock][i] + ".dae", mouth[currentBlock][i] + ".png", "static_3d", Material.DEFAULT.toString(),
								Vector3f.add(position, new Vector3f(-position.getX() + i * 40 - (units[currentBlock].length - 1) * 20, 36 - openInt / 2, 0.05f)),
								(i + 1 <= units[currentBlock].length / 2.0) ? Vector3f.EMPTY : Vector3f.FLIPPED, Vector3f.SCALE_HALF, Vector4f.COLOR_GRAY, false, true);
					}
				}
			}
			if (currentLine < 2) {
				for (int i = 0; i < 2; i++) {
					if (i <= currentLine) {
						if (i == currentLine) {
							TextRenderer.render(boxText[currentBlock][i].substring(0, charsRevealed), Vector3f.add(position, 6, -7 + i * -10, 1), rotation, scale, Vector4f.COLOR_BASE, true);
						} else {
							TextRenderer.render(boxText[currentBlock][i], Vector3f.add(position, 6, -7 + i * -10, 1), rotation, scale, Vector4f.COLOR_BASE, true);
						}
					}
				}
			} else {
				for (int i = 0; i < 2; i++) {
					if (i + currentLine - 1 < boxText[currentBlock].length) {
						if (i == 1) {
							TextRenderer.render(boxText[currentBlock][i + currentLine - 1].substring(0, charsRevealed), Vector3f.add(position, 6, -7 + i * -10, 1), rotation, scale, Vector4f.COLOR_BASE, true);
						} else {
							TextRenderer.render(boxText[currentBlock][i + currentLine - 1], Vector3f.add(position, 6, -7 + i * -10, 1), rotation, scale, Vector4f.COLOR_BASE, true);
						}
					}
				}
			}
		}
	}

	public void next() {
		if (openInt == openTo) {
			if (currentLine < boxText[currentBlock].length - 1 || charsRevealed < boxText[currentBlock][currentLine].length()) {
				if (charsRevealed == boxText[currentBlock][currentLine].length()) {
					charsRevealed = 0;
					currentLine++;
				} else {
					charsRevealed = boxText[currentBlock][currentLine].length();
				}
			} else {
				if (currentBlock < boxText.length - 1) {
					currentBlock++;
					charsRevealed = 0;
					currentLine = 0;
				} else {
					close = true;
				}
			}
		}
	}

}


//package io.itch.deltabreaker.ui;
//
//import io.itch.deltabreaker.core.Inventory;
//import io.itch.deltabreaker.core.Startup;
//import io.itch.deltabreaker.core.audio.AudioManager;
//import io.itch.deltabreaker.event.DialogueScript;
//import io.itch.deltabreaker.graphics.TextRenderer;
//import io.itch.deltabreaker.math.Vector3f;
//import io.itch.deltabreaker.math.Vector4f;
//
//public class TextBox extends UIBox {
//
//	public String[][] boxText;
//	public String[][] units;
//	public int[] highlights;
//
//	public int currentBlock = 0;
//
//	public int currentLine = 0;
//	public int charsRevealed = 0;
//	public int charTimer = 0;
//	public int[][] charTime;
//
//	public int openInt = 0;
//	public int openTo = 32;
//
//	public boolean close = false;
//
//	public TextBox(String[][] text, int[][] charTime, String[][] units, int[] highlights) {
//		super(new Vector3f(-76, -24, -80), 160, 0);
//		this.boxText = text;
//		this.charTime = charTime;
//		this.units = units;
//		this.highlights = highlights;
//	}
//
//	public TextBox(DialogueScript script) {
//		super(new Vector3f(-76, -24, -80), 160, 0);
//		boxText = script.text;
//		charTime = script.charTime;
//		units = script.units;
//		highlights = script.highlights;
//	}
//
//	public void tick() {
//		Startup.staticView.setPosition(0, 0, 0);
//		position.setY(-40 + openInt / 2);
//		if (!close) {
//			if (openInt < openTo) {
//				openInt = Math.min(openTo, openInt + 2);
//			} else {
//				if (charTimer < charTime[currentBlock][currentLine]) {
//					charTimer++;
//				} else {
//					charTimer = 0;
//					if (charsRevealed < boxText[currentBlock][currentLine].length()) {
//						charsRevealed++;
//						if (boxText[currentBlock][currentLine].charAt(charsRevealed - 1) != ' ') {
//							AudioManager.getSound("text.ogg").play(AudioManager.defaultGain, false);
//						}
//					} else {
//						if (currentLine < 1 && boxText[currentBlock].length > 1) {
//							currentLine++;
//							charsRevealed = 0;
//						}
//					}
//				}
//			}
//		} else {
//			if (openInt > 0) {
//				openInt = Math.max(0, openInt - 2);
//			}
//		}
//		height = openInt;
//	}
//
//	public void render() {
//		super.render();
//		if (!close) {
//			if (openInt == openTo) {
//				for (int i = 0; i < units[currentBlock].length; i++) {
//					if (units[currentBlock][i].length() > 0) {
//						if (highlights[currentBlock] == i) {
//							Inventory.loaded.get(units[currentBlock][i]).renderFlatPose(Vector3f.add(position, new Vector3f(-position.getX() + i * 20 - (units[currentBlock].length - 1) * 10, 36 - openInt / 2, 30)), Vector3f.SCALE_HALF,
//									Vector4f.COLOR_BASE, (i < units[currentBlock].length / 2) ? 2 : 1, 0);
//						} else {
//							Inventory.loaded.get(units[currentBlock][i]).renderFlatPose(Vector3f.add(position, new Vector3f(-position.getX() + i * 20 - (units[currentBlock].length - 1) * 10, 36 - openInt / 2, 30)), Vector3f.SCALE_HALF,
//									Vector4f.COLOR_GRAY, (i < units[currentBlock].length / 2) ? 2 : 1, 0);
//						}
//					}
//				}
//			}
//			if (currentLine < 2) {
//				for (int i = 0; i < 2; i++) {
//					if (i <= currentLine) {
//						if (i == currentLine) {
//							TextRenderer.render(boxText[currentBlock][i].substring(0, charsRevealed), Vector3f.add(position, 6, -7 + i * -10, 1), rotation, scale, Vector4f.COLOR_BASE, true);
//						} else {
//							TextRenderer.render(boxText[currentBlock][i], Vector3f.add(position, 6, -7 + i * -10, 1), rotation, scale, Vector4f.COLOR_BASE, true);
//						}
//					}
//				}
//			} else {
//				for (int i = 0; i < 2; i++) {
//					if (i + currentLine - 1 < boxText[currentBlock].length) {
//						if (i == 1) {
//							TextRenderer.render(boxText[currentBlock][i + currentLine - 1].substring(0, charsRevealed), Vector3f.add(position, 6, -7 + i * -10, 1), rotation, scale, Vector4f.COLOR_BASE, true);
//						} else {
//							TextRenderer.render(boxText[currentBlock][i + currentLine - 1], Vector3f.add(position, 6, -7 + i * -10, 1), rotation, scale, Vector4f.COLOR_BASE, true);
//						}
//					}
//				}
//			}
//		}
//	}
//
//	public void next() {
//		if (openInt == openTo) {
//			if (currentLine < boxText[currentBlock].length - 1 || charsRevealed < boxText[currentBlock][currentLine].length()) {
//				if (charsRevealed == boxText[currentBlock][currentLine].length()) {
//					charsRevealed = 0;
//					currentLine++;
//				} else {
//					charsRevealed = boxText[currentBlock][currentLine].length();
//				}
//			} else {
//				if (currentBlock < boxText.length - 1) {
//					currentBlock++;
//					charsRevealed = 0;
//					currentLine = 0;
//				} else {
//					close = true;
//				}
//			}
//		}
//	}
//
//}

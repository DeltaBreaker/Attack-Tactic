package io.itch.deltabreaker.core;

import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.nio.FloatBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import javax.swing.JOptionPane;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL40;

import io.itch.deltabreaker.ai.AIType;
import io.itch.deltabreaker.builder.dungeon.DungeonGenerator;
import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.event.DialogueScript;
import io.itch.deltabreaker.exception.MissingMetaFileException;
import io.itch.deltabreaker.graphics.BatchSorter;
import io.itch.deltabreaker.graphics.Camera;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.graphics.Model;
import io.itch.deltabreaker.graphics.ShadowMap;
import io.itch.deltabreaker.graphics.shader.Shader;
import io.itch.deltabreaker.math.AdvMath;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;
import io.itch.deltabreaker.multiprocessing.TaskThread;
import io.itch.deltabreaker.object.Unit;
import io.itch.deltabreaker.object.item.ItemProperty;
import io.itch.deltabreaker.object.tile.Tile;
import io.itch.deltabreaker.state.StateCreatorHub;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateHub;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.state.StateSplash;
import io.itch.deltabreaker.state.StateTitle;

public class Startup implements Runnable {

	private static final String META_FILE = "res/data/meta.json";
	public static final String CFG_PATH = "save";
	public static final String CFG_FILE = "settings.cfg";

	private static String title;

	public static int width = 1280, height = 720;
	public static int targetWidth = (int) Math.round(width * SettingsManager.resScaling);
	public static int targetHeight = (int) Math.round(height * SettingsManager.resScaling);

	public static double frameRate = 144.0;
	public static double frameCap = 1.0 / frameRate;
	public static double renderCap = 1.0 / SettingsManager.renderRate;

	// Threads and runnables with args
	public static Startup thread;
	public PerformanceManager performanceManager;
	public TaskThread processorEffect;
	public String[] args;

	public long window;
	public InputManager input;

	public static Camera camera;
	public static Camera shadowCamera;
	public static Camera staticView;

	public static ShadowMap shadowMap;
	public static boolean shadowMapping = false;

	public static Vector4f screenColor = new Vector4f(0, 0, 0, 0);
	public static Vector4f screenColorTarget = new Vector4f(0, 0, 0, 0);
	public static float transitionSpeed = 0.005f;
	public static float shadowBias = 0.0000075f;
	public static float corruption = 0;
	public static float corruptionTarget = 0;
	public static float corruptionSpeed = 0.001f;
	public static float seed = 0;
	public static float fog = 0;
	public static double universalAge = 0;
	public static boolean enableHaze;
	public static float depthMultiplier = 1f;

	// Used for seperate frame buffers for the bloom effect
	public int hdrFBO = 0;
	public int[] colorBuffers = new int[2];
	public int[] hdrDepthFBO = new int[2];
	public int[] pingPongFBO = new int[2];
	public int[] pingPongBuffer = new int[2];
	public boolean resize = false;
	public int deferFBO = 0;
	public int[] deferBuffers = new int[6];
	public int[] deferDepthFBO = new int[6];

	// The variables needed to render full images to the screen
	// along with the shaders needed for the bloom effect
	public int quadVAO;
	public int quadVBO;
	private Shader blur;
	private Shader bloom;
	private Shader drawImage;
	private Shader defer;

	public Startup(String[] args) {
		this.args = args;
	}

	public static void main(String[] args) {
		if (args.length > 0 && args[0].toLowerCase().equals("pack")) {
			if (args.length < 3) {
				JOptionPane.showMessageDialog(null, "Please enter the correct arguments \npack {texture/model/both} {folder}");
			} else {
				switch (args[1].toLowerCase()) {

				case "texture":
					ResourceManager.packTextures(args[2]);
					System.out.println("[Startup]: Done");
					break;

				case "model":
					ResourceManager.packModels(args[2]);
					System.out.println("[Startup]: Done");
					break;

				}
			}
		} else {
			checkArgs(args);
			System.out.println("[Startup] Starting game... ");
			thread = new Startup(args);
			new Thread(thread).start();
		}
	}

	public void init() {
		try {
			// startLogger();
			loadMetaData(META_FILE);
			SettingsManager.loadSettingsFile(CFG_PATH, CFG_FILE);

			// Initialize GLFW and setup the window
			if (!GLFW.glfwInit()) {
				System.out.println("[Startup]: GLFW failed to initialize");
				System.exit(1);
			}
			window = GLFW.glfwCreateWindow(width, height, title, 0, 0);
			GLFW.glfwMakeContextCurrent(window);
			GLFW.glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallbackI() {
				@Override
				public void invoke(long arg0, int width, int height) {
					resize(width, height);
				}
			});
			GLFW.glfwSwapInterval(0);

			// Enable OpenGL, check for compatability, and then enable features needed
			GL.createCapabilities();
			if (!GL.createCapabilities().OpenGL40) {
				System.out.println("[Startup]: GPU does not support OpenGL version 4.0, which is required. Exiting...");
				JOptionPane.showMessageDialog(null, "The installed GPU does not support OpenGL version 4.0");
				System.exit(0);
			}
			GL40.glEnable(GL40.GL_TEXTURE_2D);
			GL40.glEnable(GL40.GL_DEPTH_TEST);
			GL40.glDepthFunc(GL40.GL_LESS);
			GL40.glEnable(GL40.GL_BLEND);
			GL40.glEnable(GL40.GL_CULL_FACE);
			GL40.glCullFace(GL40.GL_BACK);
			GL40.glEnable(GL40.GL_ALPHA_TEST);
			GL40.glAlphaFunc(GL40.GL_GREATER, 0);
			GL40.glBlendFunc(GL40.GL_SRC_ALPHA, GL40.GL_ONE_MINUS_SRC_ALPHA);

			// Create textures used for bloom
			createDeferBuffer(targetWidth, targetHeight);
			createHDRBuffer(targetWidth, targetHeight);
			createPingPongBuffers(width, height);

			// Load resources
			AdvMath.loadLookupTable();
			ResourceManager.loadShaders();
			bloom = ResourceManager.shaders.get("additive_bloom");
			blur = ResourceManager.shaders.get("gaussian_blur");
			drawImage = ResourceManager.shaders.get("draw_image");
			defer = ResourceManager.shaders.get("deferred_lighting");

			ResourceManager.loadModels("res/model");
			ResourceManager.buildModels();
			ResourceManager.loadTextures("res/texture");
			ResourceManager.buildTextures();
			ResourceManager.loadModelAtlas();
			ResourceManager.loadTextureAtlas();
			AudioManager.setup();
			ResourceManager.loadAudio();
			DungeonGenerator.loadPatterns();
			DialogueScript.loadScripts("res/data/script");
			Tile.loadProperties("res/data/tile");
			ItemProperty.loadItems("res/data/item");
			Unit.loadNames("res/data/unit/names.json");
			ResourceManager.loadStates();
			ResourceManager.validateData();

			shadowMap = new ShadowMap();

			// Creates the default camera and camera used for shadows
			camera = new Camera(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), width, height, 1, 1f, 1);
			shadowCamera = new Camera(new Vector3f(0, 0, 0), new Vector3f(-90, 0, 0), width, height, 1, 1, 1);
			staticView = new Camera(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), width, height, 1, 1, 1);

			// Start co-processing threads
			performanceManager = new PerformanceManager();
			processorEffect = new TaskThread(performanceManager);
			new Thread(performanceManager).start();
			new Thread(processorEffect).start();

			// Initialize the InputManager
			input = new InputManager(performanceManager);
			GLFW.glfwSetKeyCallback(window, input.getKeyboard());
			GLFW.glfwSetJoystickCallback(input.getGamepads());

			if (Startup.thread.args.length > 0) {
				switch (Startup.thread.args[0]) {

				case "edit_level":
					StateCreatorHub.startCreating(16, 16, "village_forrest.json");
					break;

				default:
					Inventory.active.add(Unit.randomCombatUnit(-1, -1, new Vector4f(1, 1, 1, 1), 5, 0, Unit.GROWTH_PROFILES[new Random().nextInt(Unit.GROWTH_PROFILES.length)], AIType.STANDARD_DUNGEON));
					Inventory.active.add(Unit.randomCombatUnit(-1, -1, new Vector4f(1, 1, 1, 1), 5, 0, Unit.GROWTH_PROFILES[new Random().nextInt(Unit.GROWTH_PROFILES.length)], AIType.STANDARD_DUNGEON));
					Inventory.active.add(Unit.randomCombatUnit(-1, -1, new Vector4f(1, 1, 1, 1), 5, 0, Unit.GROWTH_PROFILES[new Random().nextInt(Unit.GROWTH_PROFILES.length)], AIType.STANDARD_DUNGEON));
					Inventory.active.add(Unit.randomCombatUnit(-1, -1, new Vector4f(1, 1, 1, 1), 5, 0, Unit.GROWTH_PROFILES[new Random().nextInt(Unit.GROWTH_PROFILES.length)], AIType.STANDARD_DUNGEON));
					Inventory.active.add(Unit.randomCombatUnit(-1, -1, new Vector4f(1, 1, 1, 1), 5, 0, Unit.GROWTH_PROFILES[new Random().nextInt(Unit.GROWTH_PROFILES.length)], AIType.STANDARD_DUNGEON));
					StateDungeon.startDungeon(0, args[0], 0, new Random().nextLong());
					break;

				}
			} else {
//				StateManager.swapState(StateSplash.STATE_ID);
//				StateManager.swapState(StateTitle.STATE_ID);

//				Inventory.units.add(Unit.randomCombatUnit(-1, -1, new Vector4f(1, 1, 1, 1), 5, 0, Unit.GROWTH_BALANCE, AIType.STANDARD_DUNGEON));
//				StateHub.loadMap("house");

//				StateDungeon.loadMap("bridge_test");
				StateHub.loadMap("title_scene");

				Inventory.active.add(Unit.randomCombatUnit(-1, -1, new Vector4f(1, 1, 1, 1), 5, 0, Unit.GROWTH_PROFILES[new Random().nextInt(Unit.GROWTH_PROFILES.length)], AIType.STANDARD_DUNGEON));
				Inventory.active.add(Unit.randomCombatUnit(-1, -1, new Vector4f(1, 1, 1, 1), 5, 0, Unit.GROWTH_PROFILES[new Random().nextInt(Unit.GROWTH_PROFILES.length)], AIType.STANDARD_DUNGEON));
				Inventory.active.add(Unit.randomCombatUnit(-1, -1, new Vector4f(1, 1, 1, 1), 5, 0, Unit.GROWTH_PROFILES[new Random().nextInt(Unit.GROWTH_PROFILES.length)], AIType.STANDARD_DUNGEON));
				Inventory.active.add(Unit.randomCombatUnit(-1, -1, new Vector4f(1, 1, 1, 1), 5, 0, Unit.GROWTH_PROFILES[new Random().nextInt(Unit.GROWTH_PROFILES.length)], AIType.STANDARD_DUNGEON));
				Inventory.active.add(Unit.randomCombatUnit(-1, -1, new Vector4f(1, 1, 1, 1), 5, 0, Unit.GROWTH_PROFILES[new Random().nextInt(Unit.GROWTH_PROFILES.length)], AIType.STANDARD_DUNGEON));

//				Inventory.units.add(Unit.randomCombatUnit(-1, -1, new Vector4f(1, 1, 1, 1), 5, 0, Unit.GROWTH_PROFILES[new Random().nextInt(Unit.GROWTH_PROFILES.length)], AIType.STANDARD_DUNGEON));
//				Inventory.units.add(Unit.randomCombatUnit(-1, -1, new Vector4f(1, 1, 1, 1), 5, 0, Unit.GROWTH_PROFILES[new Random().nextInt(Unit.GROWTH_PROFILES.length)], AIType.STANDARD_DUNGEON));
//				Inventory.units.add(Unit.randomCombatUnit(-1, -1, new Vector4f(1, 1, 1, 1), 5, 0, Unit.GROWTH_PROFILES[new Random().nextInt(Unit.GROWTH_PROFILES.length)], AIType.STANDARD_DUNGEON));
//				Inventory.units.add(Unit.randomCombatUnit(-1, -1, new Vector4f(1, 1, 1, 1), 5, 0, Unit.GROWTH_PROFILES[new Random().nextInt(Unit.GROWTH_PROFILES.length)], AIType.STANDARD_DUNGEON));
//				Inventory.units.add(Unit.randomCombatUnit(-1, -1, new Vector4f(1, 1, 1, 1), 5, 0, Unit.GROWTH_PROFILES[new Random().nextInt(Unit.GROWTH_PROFILES.length)], AIType.STANDARD_DUNGEON));
//				Inventory.header = "Humble Beginnings 3";
//				Inventory.loadMap = "title_scene";
//				Inventory.saveHeader(2);
//				Inventory.saveGame(2);
				StateDungeon.startDungeon(0, "flooded_forrest.json", 14, -1932052909105962160L);
//				StateDungeon.startDungeon(0, "seabed_cove.json", 14, new Random().nextLong());
			}

			GLFW.glfwShowWindow(window);

			if (SettingsManager.fullscreen) {
				GLFW.glfwSetWindowAttrib(Startup.thread.window, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
				SettingsManager.storedRes.setSize(Startup.width, Startup.height);
				int[] x = new int[1];
				int[] y = new int[1];
				GLFW.glfwGetWindowPos(Startup.thread.window, x, y);
				SettingsManager.storedPos.setLocation(x[0], y[0]);

				GLFW.glfwSetWindowPos(Startup.thread.window, 0, 0);
				GLFWVidMode vid = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
				GLFW.glfwSetWindowSize(Startup.thread.window, vid.width(), vid.height());
				resize(width, height);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void loadMetaData(String file) {
		File f = new File(file);
		try {
			if (f.exists()) {
				JSONObject jo = (JSONObject) new JSONParser().parse(new FileReader(f));

				title = (String) jo.get("window");
				StateTitle.title = (String) jo.get("title");
				StateTitle.titleIcon = (String) jo.get("title_icon");
				StateSplash.splashIcon = (String) jo.get("splash_icon");
				StateSplash.text = (String) jo.get("splash_text");
			} else {
				throw new MissingMetaFileException("Missing meta file in location: " + f.getPath());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void tick() {
		long time = System.nanoTime();
		universalAge += 1;
		seed = new Random().nextFloat();
		if (fog < 1) {
			fog += 0.00075;
		} else {
			fog = 0;
		}
		if (corruption < corruptionTarget) {
			corruption = Math.min(corruption + corruptionSpeed, corruptionTarget);
		}
		if (corruption > corruptionTarget) {
			corruption = Math.max(corruption - corruptionSpeed, corruptionTarget);
		}

		if (screenColor.getX() < screenColorTarget.getX()) {
			screenColor.setX(Math.min(screenColor.getX() + transitionSpeed, screenColorTarget.getX()));
		}
		if (screenColor.getX() > screenColorTarget.getX()) {
			screenColor.setX(Math.max(screenColor.getX() - transitionSpeed, screenColorTarget.getX()));
		}
		if (screenColor.getY() < screenColorTarget.getY()) {
			screenColor.setY(Math.min(screenColor.getY() + transitionSpeed, screenColorTarget.getY()));
		}
		if (screenColor.getY() > screenColorTarget.getY()) {
			screenColor.setY(Math.max(screenColor.getY() - transitionSpeed, screenColorTarget.getY()));
		}
		if (screenColor.getZ() < screenColorTarget.getZ()) {
			screenColor.setZ(Math.min(screenColor.getZ() + transitionSpeed, screenColorTarget.getZ()));
		}
		if (screenColor.getZ() > screenColorTarget.getZ()) {
			screenColor.setZ(Math.max(screenColor.getZ() - transitionSpeed, screenColorTarget.getZ()));
		}
		if (screenColor.getW() < screenColorTarget.getW()) {
			screenColor.setW(Math.min(screenColor.getW() + transitionSpeed, screenColorTarget.getW()));
		}
		if (screenColor.getW() > screenColorTarget.getW()) {
			screenColor.setW(Math.max(screenColor.getW() - transitionSpeed, screenColorTarget.getW()));
		}

		// Used for repeat inputs (ie. the cursor movement)
		GLFW.glfwPollEvents();
		input.checkGamepadState();
		if (input.pressed) {
			if (input.repeatTimer < InputManager.repeatDelay) {
				input.repeatTimer++;
			} else {
				if (input.keyTimer < InputManager.keyTime) {
					input.keyTimer++;
				} else {
					input.keyTimer = 0;
					input.repeatActions();
				}
			}
		} else {
			input.repeatTimer = 0;
		}

		AudioManager.tick();
		StateManager.tick();
		camera.tick();
		shadowCamera.tick();
		staticView.tick();
		performanceManager.updates++;
		performanceManager.updateTime += (System.nanoTime() - time) / 1000000.0;
	}

	public void render() {
		long time = System.nanoTime();
		if (resize) {
			resize(width, height);
			resize = false;
		}
		if (SettingsManager.resScaling * width != targetWidth) {
			updateResolution();
		}
		for (Shader s : ResourceManager.shaders.values()) {
			s.setStaticUniforms = true;
		}

		// Prepare for rendering to the shadow map
		GL40.glBindFramebuffer(GL40.GL_FRAMEBUFFER, shadowMap.depthMapFBO);
		GL40.glViewport(0, 0, SettingsManager.shadowMapSize, SettingsManager.shadowMapSize);
		GL40.glClear(GL40.GL_DEPTH_BUFFER_BIT);
		BatchSorter.clear();
		shadowMapping = true;

		// Load models into the batch sorter and render
		StateManager.render();
		BatchSorter.renderShadow();

		// Prepare to actually render the scene
		shadowMapping = false;
		GL40.glBindFramebuffer(GL40.GL_FRAMEBUFFER, deferFBO);
		GL40.glViewport(0, 0, targetWidth, targetHeight);
		GL40.glClear(GL40.GL_DEPTH_BUFFER_BIT);

//		GL40.glClearBufferfv(GL40.GL_COLOR, 0, new float[] { screenColor.getX(), screenColor.getY(), screenColor.getZ(), 1 });
//		GL40.glClearBufferfv(GL40.GL_COLOR, 1, new float[] { 0, 1, 0, 1 });
//		GL40.glClearBufferfv(GL40.GL_COLOR, 2, new float[] { camera.position.getX(), 1, camera.position.getZ(), 1 });
//		GL40.glClearBufferfv(GL40.GL_COLOR, 3, new float[] { 0.75f, 1, 1, 1 });
//		GL40.glClearBufferfv(GL40.GL_COLOR, 4, new float[] { 0, 0, 0, 1 });
//		GL40.glClearBufferfv(GL40.GL_COLOR, 5, new float[] { 0, 0, 0, 1 });

		BatchSorter.render();

		GL40.glBindFramebuffer(GL40.GL_FRAMEBUFFER, hdrFBO);
		GL40.glViewport(0, 0, targetWidth, targetHeight);
		GL40.glClear(GL40.GL_DEPTH_BUFFER_BIT);
		GL40.glClear(GL40.GL_COLOR_BUFFER_BIT);

		defer.bind();
		defer.use(0, null);

		for (int i = 0; i < deferBuffers.length; i++) {
			GL40.glActiveTexture(GL40.GL_TEXTURE0 + i);
			GL40.glBindTexture(GL40.GL_TEXTURE_2D, deferBuffers[i]);
		}

		renderQuad();

		// This blurs the separate texture created for the bloom effect
		GL40.glViewport(0, 0, width, height);
		GL40.glActiveTexture(GL40.GL_TEXTURE0 + 0);
		if (SettingsManager.enableBloom) {
			boolean horizontal = true;
			blur.bind();
			blur.setUniform("sampler", 0);
			for (int i = 0; i < SettingsManager.bloomFidelity; i++) {
				GL40.glBindFramebuffer(GL40.GL_FRAMEBUFFER, pingPongFBO[horizontal ? 1 : 0]);

				blur.setUniform("horizontal", horizontal);

				GL40.glBindTexture(GL40.GL_TEXTURE_2D, i == 0 ? colorBuffers[1] : pingPongBuffer[!horizontal ? 1 : 0]);

				renderQuad();
				horizontal = !horizontal;
			}

			// Prepare to render to the actual screen
			GL40.glBindFramebuffer(GL40.GL_FRAMEBUFFER, 0);
			GL40.glClear(GL40.GL_COLOR_BUFFER_BIT | GL40.GL_DEPTH_BUFFER_BIT);
			GL40.glBindTexture(GL40.GL_TEXTURE_2D, colorBuffers[0]);
			GL40.glActiveTexture(GL40.GL_TEXTURE0 + 1);
			GL40.glBindTexture(GL40.GL_TEXTURE_2D, pingPongBuffer[0]);
			GL40.glActiveTexture(GL40.GL_TEXTURE0 + 3);
			GL40.glBindTexture(GL40.GL_TEXTURE_2D, deferBuffers[5]);
			bloom.bind();
			bloom.use(0, null);
		} else {
			GL40.glBindFramebuffer(GL40.GL_FRAMEBUFFER, 0);
			GL40.glBindTexture(GL40.GL_TEXTURE_2D, colorBuffers[0]);
			GL40.glActiveTexture(GL40.GL_TEXTURE0 + 1);
			GL40.glBindTexture(GL40.GL_TEXTURE_2D, deferBuffers[5]);
			drawImage.bind();
			drawImage.use(0, null);
		}

		renderQuad();

		GL40.glClear(GL40.GL_DEPTH_BUFFER_BIT);
		BatchSorter.renderStatic();

		// Used to fade the screen
		ResourceManager.models.get("fade.dae").render(Vector3f.SCREEN_POSITION, Vector3f.EMPTY, Vector3f.SCALE_FULL, ResourceManager.textures.get("fade.png"), ResourceManager.shaders.get("static_3d"), Material.MATTE, screenColor, false);

		GLFW.glfwSwapBuffers(window);
		performanceManager.renders++;
		performanceManager.renderTime += (System.nanoTime() - time) / 1000000.0;
	}

	// This simply renders a full screen quad
	public void renderQuad() {
		if (quadVAO == 0) {
			createQuad();
		}
		GL40.glBindVertexArray(quadVAO);
		GL40.glDrawArrays(GL40.GL_TRIANGLE_STRIP, 0, 4);
		GL40.glBindVertexArray(0);
	}

	@Override
	public void run() {
		init();

		double time = getTime();
		double unprocessed = 0;
		double unrendered = 0;

		while (!GLFW.glfwWindowShouldClose(window)) {
			boolean shouldRender = false;
			double time2 = getTime();
			double passed = time2 - time;
			time = time2;
			unprocessed += passed;
			unrendered += passed;

			while (unprocessed >= frameCap) {
				unprocessed -= frameCap;
				tick();
				shouldRender = true;
			}
			if (shouldRender && unrendered >= renderCap) {
				unrendered -= renderCap;
				render();
			}

			int error = GL40.glGetError();

			switch (error) {

			case GL40.GL_INVALID_ENUM:
				System.out.println("[Startup]: GL Error: Invalid enum");
				break;

			case GL40.GL_INVALID_VALUE:
				System.out.println("[Startup]: GL Error: Invalid value");
				break;

			case GL40.GL_INVALID_OPERATION:
				System.out.println("[Startup]: GL Error: Invalid opperation");
				break;

			case GL40.GL_STACK_OVERFLOW:
				System.out.println("[Startup]: GL Error: Stack overflow");
				break;

			case GL40.GL_STACK_UNDERFLOW:
				System.out.println("[Startup]: GL Error: Stack underflow");
				break;

			case GL40.GL_OUT_OF_MEMORY:
				System.out.println("[Startup]: GL Error: Out of memory");
				break;

			case GL40.GL_INVALID_FRAMEBUFFER_OPERATION:
				System.out.println("[Startup]: GL Error: Invalid framebuffer operation");
				break;

			}
		}
		destroy();
	}

	// Called when the window is resized
	// Updates the projection matrices and frame buffer sizes
	public void resize(int width, int height) {
		try {
			Startup.width = width;
			Startup.height = height;
			targetWidth = (int) Math.round(width * SettingsManager.resScaling);
			targetHeight = (int) Math.round(height * SettingsManager.resScaling);

			createPingPongBuffers(width, height);
			camera.updateProjection(targetWidth, targetHeight);
			shadowCamera.updateProjection(targetWidth, targetHeight);
			createDeferBuffer(targetWidth, targetHeight);
			createHDRBuffer(targetWidth, targetHeight);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateResolution() {
		try {
			targetWidth = (int) Math.round(width * SettingsManager.resScaling);
			targetHeight = (int) Math.round(height * SettingsManager.resScaling);

			camera.updateProjection(targetWidth, targetHeight);
			shadowCamera.updateProjection(targetWidth, targetHeight);
			createDeferBuffer(targetWidth, targetHeight);
			createHDRBuffer(targetWidth, targetHeight);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Creates the data needed to render a quad to the full screen
	private void createQuad() {
		float quadVertices[] = { -1.0f, 1.0f, 0.0f, 0.0f, 1.0f, -1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, -1.0f, 0.0f, 1.0f, 0.0f, };
		quadVAO = GL40.glGenVertexArrays();
		quadVBO = GL40.glGenBuffers();
		GL40.glBindVertexArray(quadVAO);
		GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, quadVBO);
		GL40.glBufferData(GL40.GL_ARRAY_BUFFER, Model.createFloatBuffer(quadVertices), GL40.GL_STATIC_DRAW);
		GL40.glEnableVertexAttribArray(0);
		GL40.glVertexAttribPointer(0, 3, GL40.GL_FLOAT, false, 5 * 4, 0);
		GL40.glEnableVertexAttribArray(1);
		GL40.glVertexAttribPointer(1, 2, GL40.GL_FLOAT, false, 5 * 4, 3 * 4);
	}

	// Sets up the two buffers used for the bloom effect
	private void createHDRBuffer(int width, int height) {
		GL40.glDeleteFramebuffers(hdrFBO);
		GL40.glDeleteTextures(colorBuffers);
		GL40.glDeleteRenderbuffers(hdrDepthFBO);

		hdrFBO = GL40.glGenFramebuffers();
		GL40.glBindFramebuffer(GL40.GL_FRAMEBUFFER, hdrFBO);

		GL40.glGenTextures(colorBuffers);
		for (int i = 0; i < 2; i++) {
			GL40.glBindTexture(GL40.GL_TEXTURE_2D, colorBuffers[i]);
			GL40.glTexImage2D(GL40.GL_TEXTURE_2D, 0, GL40.GL_RGBA16F, width, height, 0, GL40.GL_RGBA, GL40.GL_FLOAT, (FloatBuffer) null);
			GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_MIN_FILTER, GL40.GL_LINEAR);
			GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_MAG_FILTER, GL40.GL_LINEAR);
			GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_WRAP_S, GL40.GL_CLAMP_TO_EDGE);
			GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_WRAP_T, GL40.GL_CLAMP_TO_EDGE);
			GL40.glFramebufferTexture2D(GL40.GL_FRAMEBUFFER, GL40.GL_COLOR_ATTACHMENT0 + i, GL40.GL_TEXTURE_2D, colorBuffers[i], 0);

			hdrDepthFBO[i] = GL40.glGenRenderbuffers();
			GL40.glBindRenderbuffer(GL40.GL_RENDERBUFFER, hdrDepthFBO[i]);
			GL40.glRenderbufferStorage(GL40.GL_RENDERBUFFER, GL40.GL_DEPTH_COMPONENT, width, height);
			GL40.glFramebufferRenderbuffer(GL40.GL_FRAMEBUFFER, GL40.GL_DEPTH_ATTACHMENT, GL40.GL_RENDERBUFFER, hdrDepthFBO[i]);
		}

		int[] attachments = { GL40.GL_COLOR_ATTACHMENT0, GL40.GL_COLOR_ATTACHMENT1 };
		GL40.glDrawBuffers(attachments);
	}

	// Sets up the two buffers used for the bloom effect
	private void createDeferBuffer(int width, int height) {
		GL40.glDeleteFramebuffers(deferFBO);
		GL40.glDeleteTextures(deferBuffers);
		GL40.glDeleteRenderbuffers(deferDepthFBO);

		deferFBO = GL40.glGenFramebuffers();
		GL40.glBindFramebuffer(GL40.GL_FRAMEBUFFER, deferFBO);

		GL40.glGenTextures(deferBuffers);
		for (int i = 0; i < deferBuffers.length; i++) {
			GL40.glBindTexture(GL40.GL_TEXTURE_2D, deferBuffers[i]);
			GL40.glTexImage2D(GL40.GL_TEXTURE_2D, 0, GL40.GL_RGBA16F, width, height, 0, GL40.GL_RGBA, GL40.GL_FLOAT, (FloatBuffer) null);
			GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_MIN_FILTER, GL40.GL_LINEAR);
			GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_MAG_FILTER, GL40.GL_LINEAR);
			GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_WRAP_S, GL40.GL_CLAMP_TO_EDGE);
			GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_WRAP_T, GL40.GL_CLAMP_TO_EDGE);
			GL40.glFramebufferTexture2D(GL40.GL_FRAMEBUFFER, GL40.GL_COLOR_ATTACHMENT0 + i, GL40.GL_TEXTURE_2D, deferBuffers[i], 0);

			deferDepthFBO[i] = GL40.glGenRenderbuffers();
			GL40.glBindRenderbuffer(GL40.GL_RENDERBUFFER, deferDepthFBO[i]);
			GL40.glRenderbufferStorage(GL40.GL_RENDERBUFFER, GL40.GL_DEPTH_COMPONENT, width, height);
			GL40.glFramebufferRenderbuffer(GL40.GL_FRAMEBUFFER, GL40.GL_DEPTH_ATTACHMENT, GL40.GL_RENDERBUFFER, deferDepthFBO[i]);
		}

		int[] attachments = { GL40.GL_COLOR_ATTACHMENT0, GL40.GL_COLOR_ATTACHMENT1, GL40.GL_COLOR_ATTACHMENT2, GL40.GL_COLOR_ATTACHMENT3, GL40.GL_COLOR_ATTACHMENT4, GL40.GL_COLOR_ATTACHMENT5 };
		GL40.glDrawBuffers(attachments);
	}

	// Sets up the two buffers for the gaussian blur since it uses two phases of
	// blur
	private void createPingPongBuffers(int width, int height) {
		GL40.glDeleteFramebuffers(pingPongFBO);
		GL40.glDeleteTextures(pingPongBuffer);

		GL40.glGenFramebuffers(pingPongFBO);
		GL40.glGenTextures(pingPongBuffer);
		for (int i = 0; i < 2; i++) {
			GL40.glBindFramebuffer(GL40.GL_FRAMEBUFFER, pingPongFBO[i]);
			GL40.glBindTexture(GL40.GL_TEXTURE_2D, pingPongBuffer[i]);
			GL40.glTexImage2D(GL40.GL_TEXTURE_2D, 0, GL40.GL_RGB16F, width, height, 0, GL40.GL_RGB, GL40.GL_FLOAT, (FloatBuffer) null);
			GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_MIN_FILTER, GL40.GL_LINEAR);
			GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_MAG_FILTER, GL40.GL_LINEAR);
			GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_WRAP_S, GL40.GL_CLAMP_TO_EDGE);
			GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_WRAP_T, GL40.GL_CLAMP_TO_EDGE);
			GL40.glFramebufferTexture2D(GL40.GL_FRAMEBUFFER, GL40.GL_COLOR_ATTACHMENT0, GL40.GL_TEXTURE_2D, pingPongBuffer[i], 0);
		}
	}

	public static double getTime() {
		return (double) System.nanoTime() / 1000000000L;
	}

	// Ran upon window closing
	public void destroy() {
		AudioManager.cleanUp();
		SettingsManager.destroy();
		performanceManager.running = false;
		processorEffect.running = false;
		GLFW.glfwDestroyWindow(window);
	}

	public static void startLogger() {
		File f = new File("logs");
		if (!f.exists()) {
			f.mkdirs();
		}
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
		File log = new File(f + "/" + dtf.format(LocalDateTime.now()) + ".log");
		try {
			PrintStream out = new PrintStream(log);
			System.setOut(new ConsoleLogger(out, System.out));
			System.setErr(new ConsoleLogger(out, System.err));
			System.out.println("[Startup]: Started console logger");
		} catch (Exception e) {
			System.out.println("[Startup]: There was an error creating the PrintStream");
		}
	}

	private static void checkArgs(String[] args) {
		for (String s : args) {
			switch (s) {

			case "-log":
				startLogger();
				break;

			case "-verbose":
				SettingsManager.consolePerformacneOutput = true;
				break;

			}
		}
	}

}

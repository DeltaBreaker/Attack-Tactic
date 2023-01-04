package io.itch.deltabreaker.core;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.lwjgl.opengl.GL11;

import io.itch.deltabreaker.multiprocessing.TaskThread;
import io.itch.deltabreaker.state.StateManager;

public class PerformanceManager implements Runnable {

	public static boolean developerMode = false;
	public static boolean checkForCrash = true;
	
	public float ups;
	public float fps;
	public float edups;

	public int updates = 0;
	public int renders = 0;
	public int effectDesyncUpdates;

	public double updateTime = 0;
	public double renderTime = 0;
	public double rawRenderTime = 0;
	public double effectDesyncUpdateTime = 0;

	public double averageUpdateTime = 0;
	public double averageRenderTime = 0;
	public double averageRawRenderTime = 0;
	public double averageEffectDesyncUpdateTime = 0;
	public double tt = 0;
	public double rt = 0;
	public double rrt = 0;
	public double edut = 0;

	public long maxMemory = 1;
	public long freeMemory = 0;

	public boolean running = true;

	private Runtime runtime;
	private String gpuVendor;
	private double[] perfHistory = new double[15];
	private double historyTotal = 0;

	private JFrame frame;
	
	public PerformanceManager() {
		runtime = Runtime.getRuntime();
		gpuVendor = GL11.glGetString(GL11.GL_VENDOR) + " " + GL11.glGetString(GL11.GL_RENDERER) + " | Driver Version: " + GL11.glGetString(GL11.GL_VERSION);

		Arrays.fill(perfHistory, 1);

		frame = new JFrame(gpuVendor);
		frame.setSize(640, 440);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setResizable(false);

		JPanel panel = new JPanel() {
			private static final long serialVersionUID = 6043212521861219700L;

			@Override
			public void paintComponent(Graphics g) {
				double total = tt + rt;

				g.setColor(Color.black);
				g.fillRect(0, 0, frame.getWidth(), frame.getHeight());

				g.setColor(Color.magenta);
				g.drawString("TPS: " + (int) ups, 5, 15);
				g.drawString("Average Update Time: " + tt + "ms", 5, 35);

				g.fillArc(20, 175, 150, 150, 0, 360);

				g.setColor(Color.orange);
				g.drawString("FPS: " + (int) fps, 5, 75);
				g.drawString("Average Render Time: " + rt + "ms", 5, 95);
				g.drawString("Average Raw Render Time: " + rrt + "ms", 5, 115);

				int rtend = (int) Math.round(360 / total * rt);
				g.fillArc(20, 175, 150, 150, 0, rtend);

				g.setColor(Color.yellow);
				g.drawString("Memory Usage: " + (int) (((double) freeMemory / maxMemory) * 10000) / 100.0 + "%", 200, 15);
				g.drawString(freeMemory + "MB / " + maxMemory + "MB", 200, 35);
				g.drawString("Average Total Frame Time: " + ((int) ((tt + rt) * 10000)) / 10000.0, 5, 155);

				g.drawString("Resolution: " + Startup.width + " x " + Startup.height + " (" + (int) (Startup.width * SettingsManager.resScaling) + " x " + (int) (Startup.height * SettingsManager.resScaling) + ")", 400, 15);
				g.drawString("Light Count: " + StateManager.currentState.lights.size(), 400, 35);
				g.drawString("Effect Count: " + StateManager.currentState.effects.size(), 400, 55);
				g.drawString("Loaded Unit Count: " + Inventory.loaded.size(), 400, 75);
				g.drawString("Performance History: " + (int) (historyTotal * 10000) / 10000.0, 400, 95);

				int memUse = (int) Math.round((((double) freeMemory / maxMemory) * 360));
				g.fillArc(200, 175, 150, 150, 0, memUse);

				g.setColor(Color.white);
				g.drawArc(200, 175, 150, 150, 0, 360);
				g.drawArc(20, 175, 150, 150, 0, 360);

				g.setColor(Color.blue);
				g.drawString("EDUPS: " + (int) edups, 5, 355);
				g.drawString("Average Task Update Time: " + edut + "ms", 5, 375);
				g.drawString("Tasks: " + TaskThread.taskCount(), 5, 395);
			}
		};
		panel.setSize(640, 360);
		frame.add(panel);

		System.out.println("[PerformanceManager]: GPU: " + gpuVendor);
	}

	public void toggleWindow() {
		frame.requestFocus();
		frame.setVisible(!frame.isVisible());
	}

	@Override
	public void run() {
		while (running) {
			try {
				ups = updates;
				fps = renders;
				edups = effectDesyncUpdates;

				averageUpdateTime = (double) (updateTime / updates);
				averageRenderTime = (double) (renderTime / renders);
				averageRawRenderTime = (double) (rawRenderTime / renders);
				averageEffectDesyncUpdateTime = (double) (effectDesyncUpdateTime / effectDesyncUpdates);
				tt = (int) (averageUpdateTime * 10000) / 10000.0;
				rt = (int) (averageRenderTime * 10000) / 10000.0;
				rrt = (int) (averageRawRenderTime * 10000) / 10000.0;
				edut = (int) (averageEffectDesyncUpdateTime * 10000) / 10000.0;

				updates = 0;
				updateTime = 0;
				renders = 0;
				renderTime = 0;
				rawRenderTime = 0;
				effectDesyncUpdates = 0;
				effectDesyncUpdateTime = 0;

				for (int i = 0; i < perfHistory.length - 1; i++) {
					perfHistory[i] = perfHistory[i + 1];
				}
				perfHistory[perfHistory.length - 1] = ups + fps + tt + rt;

				historyTotal = 0;
				for (double d : perfHistory) {
					historyTotal += d;
				}

				maxMemory = runtime.maxMemory() / 1048576;
				freeMemory = runtime.freeMemory() / 1048576;
				
				if (SettingsManager.consolePerformacneOutput) {
					System.out.println("[PerformanceManager]: TPS: " + (int) ups + " | FPS: " + (int) fps + " | TT: " + tt + "ms | RT: " + rt + "ms | Memory Usage: " + (int) (((double) freeMemory / maxMemory) * 10000) / 100.0 + "% of "
							+ maxMemory + "MB" + " | Light Count: " + StateManager.currentState.lights.size() + " | Resolution: " + Startup.width + " x " + Startup.height + " - " + Startup.targetWidth + " x " + Startup.targetHeight
							+ " | Performance History: " + (int) (historyTotal * 10000) / 10000.0);
					System.out.println("[PerformanceManager]: EDUPS: " + (int) edups + " | EDUT: " + edut + "ms" + " | Task Count: " + TaskThread.taskCount());
				}

				if (historyTotal < 0.000001 && checkForCrash) {
					JOptionPane.showMessageDialog(null, "Performance history was empty. A crash or halt was probable. Exiting...");
					System.out.println("[PerformanceManager]: Performance history was empty. A crash or halt was probable. Exiting...");
					System.exit(0);
				}

				frame.repaint();
				Inventory.playtime++;
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		frame.dispose();
	}

}

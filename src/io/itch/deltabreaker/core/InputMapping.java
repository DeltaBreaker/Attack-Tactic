package io.itch.deltabreaker.core;

public enum InputMapping {

	UP {
		@Override
		public void reset() {
			keyCode = 265;
			buttonCode = 11;
		}
	},

	DOWN {
		@Override
		public void reset() {
			keyCode = 264;
			buttonCode = 13;
		}
	},

	LEFT {
		@Override
		public void reset() {
			keyCode = 263;
			buttonCode = 14;
		}
	},

	RIGHT {
		@Override
		public void reset() {
			keyCode = 262;
			buttonCode = 12;
		}
	},

	CONFIRM {
		@Override
		public void reset() {
			keyCode = 90;
			buttonCode = 0;
		}
	},

	BACK {
		@Override
		public void reset() {
			keyCode = 88;
			buttonCode = 1;
		}
	},

	WEAPON_LEFT {
		@Override
		public void reset() {
			keyCode = 65;
			buttonCode = 4;
		}
	},

	WEAPON_RIGHT {
		@Override
		public void reset() {
			keyCode = 83;
			buttonCode = 5;
		}
	},

	HIGHLIGHT {
		@Override
		public void reset() {
			keyCode = 68;
			buttonCode = 3;
		}
	},

	SHOW_INFO {
		@Override
		public void reset() {
			keyCode = 292;
			buttonCode = -1;
		}
	},

	SHOW_SETTINGS {
		@Override
		public void reset() {
			keyCode = 293;
			buttonCode = -1;
		}
	},

	MISC {
		@Override
		public void reset() {
			keyCode = 294;
			buttonCode = -1;
		}
	},

	ADD {
		@Override
		public void reset() {
			keyCode = 81;
			buttonCode = -1;
		}
	},

	REMOVE {
		@Override
		public void reset() {
			keyCode = 87;
			buttonCode = -1;
		}
	},

	JOYSTICK {
		@Override
		public void reset() {
			// Empty
		}
	},

	DEFAULT {
		@Override
		public void reset() {
			keyCode = -1;
			buttonCode = -1;
		}
	};

	public abstract void reset();

	public int keyCode;
	public int buttonCode;
	private float axisX, axisY = 0;

	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}

	public void setButtonCode(int buttonCode) {
		this.buttonCode = buttonCode;
	}

	public void setAxes(float axisX, float axisY) {
		this.axisX = axisX;
		this.axisY = axisY;
	}

	public float[] getAxes() {
		return new float[] { axisX, axisY };
	}

	public static InputMapping getActionFromKey(int key) {
		for (InputMapping i : values()) {
			if (key == i.keyCode) {
				return i;
			}
		}
		return DEFAULT;
	}

	public static InputMapping getActionFromButton(int button) {
		for (InputMapping i : values()) {
			if (button == i.buttonCode) {
				return i;
			}
		}
		return DEFAULT;
	}
}
package com.senstore.alice.menus;
/**
 * @author Antony Evans Antony@senstore.com
 * 
 */
public enum BodyGuide {

	HEAD {

		@Override
		public String userFriendlyName() {
			return "Head & Neck";
		}


		@Override
		public String result() {
			return "Head and neck";
		}

	},
	TORSO {

		@Override
		public String userFriendlyName() {
			return "Torso";
		}


		@Override
		public String result() {
			return "Torso";
		}

	},
	LIMBS {

		@Override
		public String userFriendlyName() {
			return "Limbs (arms and legs)";
		}


		@Override
		public String result() {
			return "Limbs (arms and legs)";
		}

	},
	SEX_WOMEN {

		@Override
		public String userFriendlyName() {
			return "Sex organs (women)";
		}


		@Override
		public String result() {
			return "Sex organs (women)";
		}

	},
	SEX_MEN {

		@Override
		public String userFriendlyName() {
			return "Sex organs (men)";
		}


		@Override
		public String result() {
			return "Sex organs (men)";
		}

	},
	MENTAL {

		@Override
		public String userFriendlyName() {
			return "Mental / emotional";
		}


		@Override
		public String result() {
			return "Mental / emotional";
		}

	},
	GENERAL_ADULT {

		@Override
		public String userFriendlyName() {
			return "General (adults)";
		}


		@Override
		public String result() {
			return "General (adults)";
		}

	},
	GENERAL_CHILD {

		@Override
		public String userFriendlyName() {
			return "General (children)";
		}


		@Override
		public String result() {
			return "General (children)";
		}

	};

	public abstract String userFriendlyName();

	public abstract String result();

}

package com.senstore.alice.menus;
/**
 * @author Antony Evans Antony@senstore.com
 * 
 */
public enum DemographicGuide {

	CHILDREN {

		@Override
		public String userFriendlyName() {
			return "Children";
		}


		@Override
		public String result() {
			return "Children";
		}

	},
	WOMEN {

		@Override
		public String userFriendlyName() {
			return "Women";
		}


		@Override
		public String result() {
			return "Women";
		}

	},
	MEN {

		@Override
		public String userFriendlyName() {
			return "Men";
		}


		@Override
		public String result() {
			return "Men";
		}

	},
	ADULTS {

		@Override
		public String userFriendlyName() {
			return "Adults";
		}


		@Override
		public String result() {
			return "Adults";
		}

	};

	public abstract String userFriendlyName();

	public abstract String result();

}

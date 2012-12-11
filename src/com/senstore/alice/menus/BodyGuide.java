package com.senstore.alice.menus;
/**
 * @author Antony Evans Antony@senstore.com
 * 
 */
public enum BodyGuide {

	HEAD {

		@Override
		public String userFriendlyName() {
			return "Head";
		}


		@Override
		public String result() {
			return "Head";
		}

	},
	SHOULDERS {

		@Override
		public String userFriendlyName() {
			return "Shoulders";
		}


		@Override
		public String result() {
			return "Shoulders";
		}

	};

	public abstract String userFriendlyName();

	public abstract String result();

}

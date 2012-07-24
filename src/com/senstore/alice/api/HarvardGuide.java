package com.senstore.alice.api;
/**
 * @author Muniu Kariuki - muniu@bityarn.co.ke
 * 
 */
public enum HarvardGuide {

	BIRTH_CONTROL_WOMEN {

		@Override
		public String officialName() {
			return "Birth Control (Contraception) for Women";
		}

		@Override
		public String userFriendlyName() {
			return "Birth Control";
		}

		@Override
		public String guideName() {
			return "birthControlForWomen";
		}

		@Override
		public String startInput() {
			return "birthcontrol_oct3";
		}

	};

	public abstract String officialName();

	public abstract String userFriendlyName();

	public abstract String guideName();

	public abstract String startInput();

}

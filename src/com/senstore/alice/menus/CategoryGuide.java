package com.senstore.alice.menus;
/**
 * @author Antony Evans Antony@senstore.com
 * 
 */
public enum CategoryGuide {

	COMMON {

		@Override
		public String userFriendlyName() {
			return "Common Concerns";
		}


		@Override
		public String result() {
			return "Common";
		}

	},
	GASTRIC {

		@Override
		public String userFriendlyName() {
			return "Gastric Issues";
		}


		@Override
		public String result() {
			return "Gastric";
		}

	},
	MEN_SEX {

		@Override
		public String userFriendlyName() {
			return "Men's Sexual Health";
		}


		@Override
		public String result() {
			return "MensSexualHealth";
		}

	},
	MENTAL {

		@Override
		public String userFriendlyName() {
			return "Mental/emotional";
		}


		@Override
		public String result() {
			return "Mental";
		}

	},
	PAIN {

		@Override
		public String userFriendlyName() {
			return "Pain";
		}


		@Override
		public String result() {
			return "Pain";
		}

	},
	PREGNANT {

		@Override
		public String userFriendlyName() {
			return "Pregnancy";
		}


		@Override
		public String result() {
			return "Pregnancy";
		}

	},
	WOMEN_SEX {

		@Override
		public String userFriendlyName() {
			return "Women's Sexual Health";
		}


		@Override
		public String result() {
			return "WomensSexualHealth";
		}

	};

	public abstract String userFriendlyName();

	public abstract String result();

}

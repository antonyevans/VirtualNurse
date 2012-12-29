package com.senstore.alice.menus;
/**
 * @author Antony Evans Antony@senstore.com
 * 
 */
public enum MenuGuide {

	BODY_PART {

		@Override
		public String userFriendlyName() {
			return "Body region";
		}


		@Override
		public String result() {
			return "BodyPart";
		}

	},
	CATEGORY {

		@Override
		public String userFriendlyName() {
			return "By Category";
		}


		@Override
		public String result() {
			return "Category";
		}

	},
	DEMOGRAPHIC {

		@Override
		public String userFriendlyName() {
			return "By Demographic";
		}


		@Override
		public String result() {
			return "Demographic";
		}

	},
	YOUR_COLLECTION {

		@Override
		public String userFriendlyName() {
			return "In your collection";
		}


		@Override
		public String result() {
			return "Owned";
		}

	},
	ALL_GUIDES {

		@Override
		public String userFriendlyName() {
			return "All Guides";
		}


		@Override
		public String result() {
			return "All";
		}

	};

	public abstract String userFriendlyName();

	public abstract String result();

}

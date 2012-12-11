package com.senstore.alice.api;
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
	CATAGORY {

		@Override
		public String userFriendlyName() {
			return "By Catagory";
		}


		@Override
		public String result() {
			return "Catagory";
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

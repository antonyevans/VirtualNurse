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

	},
	BLEEDING_MENSTRUAL_PERIODS {

		@Override
		public String officialName() {

			return "Bleeding Between Menstrual Periods";
		}

		@Override
		public String userFriendlyName() {

			return "Bleeding Between Periods";
		}

		@Override
		public String guideName() {

			return "bleedingBetweenMenstrualPeriods";
		}

		@Override
		public String startInput() {

			return "bleedingbetweenmenstrualperiods";
		}

	},
	BREAST_LUMPS {

		@Override
		public String officialName() {

			return "Breast Lumps";
		}

		@Override
		public String userFriendlyName() {

			return "Breast Lumps";
		}

		@Override
		public String guideName() {

			return "breastLumps";
		}

		@Override
		public String startInput() {

			return "breastlumps";
		}

	},
	HEADACHE_IN_PREGNANCY {

		@Override
		public String officialName() {

			return "Headache in Pregnancy";
		}

		@Override
		public String userFriendlyName() {

			return "Headache in Pregnancy";
		}

		@Override
		public String guideName() {

			return "headacheinpregnancyfinal";
		}

		@Override
		public String startInput() {

			return "headacheinpregnancy_sept9";
		}

	},

	HEAVY_MENSTRUAL_PERIODS {

		@Override
		public String officialName() {

			return "Heavy Menstrual Periods";
		}

		@Override
		public String userFriendlyName() {

			return "Heavy Menstrual Periods";
		}

		@Override
		public String guideName() {

			return "heavyMenstrualPeriods";
		}

		@Override
		public String startInput() {

			return "heavymenstrualperiods";
		}

	},
	MISSED_IRREGULAR_MENSTRUAL_PERIODS {

		@Override
		public String officialName() {

			return "Missed or Irregular Menstrual Periods";
		}

		@Override
		public String userFriendlyName() {

			return "Missed or Irregular Periods";
		}

		@Override
		public String guideName() {

			return "missedOrIrregularMenstrualPeriods";
		}

		@Override
		public String startInput() {

			return "missedorirregularmenstrualperiods";
		}

	},
	PAINFUL_MENSTRUAL_CRAMPS {

		@Override
		public String officialName() {

			return "Painful Menstrual Cramps";
		}

		@Override
		public String userFriendlyName() {

			return "Painful Period Cramps";
		}

		@Override
		public String guideName() {

			return "painfulMenstrualCramps";
		}

		@Override
		public String startInput() {

			return "painfulmenstrualcramps";
		}

	},
	VAGINAL_DISCHARGE_ITCHING_IRRITATION {

		@Override
		public String officialName() {

			return "Vaginal Discharge, Itching or Irritation";
		}

		@Override
		public String userFriendlyName() {

			return "Vaginal Discharge, Itching or Irritation";
		}

		@Override
		public String guideName() {

			return "vaginaldischargeitchingorirritation";
		}

		@Override
		public String startInput() {

			return "vaginaldischargeitchingorirritation";
		}

	},
	VAGINAL_PAIN_DISCOMFORT {

		@Override
		public String officialName() {

			return "Vaginal Pain or Discomfort";
		}

		@Override
		public String userFriendlyName() {

			return "Vaginal Pain or Discomfort";
		}

		@Override
		public String guideName() {

			return "vaginalpainordiscomfort";
		}

		@Override
		public String startInput() {

			return "vaginalpainordiscomfort";
		}

	},
	VAGINAL_SORES_LUMPS {

		@Override
		public String officialName() {

			return "Vaginal Sores and Lumps";
		}

		@Override
		public String userFriendlyName() {

			return "Vaginal Sores and Lumps";
		}

		@Override
		public String guideName() {

			return "vaginalsoresandlumps";
		}

		@Override
		public String startInput() {

			return "vaginalsoresandlumps";
		}

	};

	public abstract String officialName();

	public abstract String userFriendlyName();

	public abstract String guideName();

	public abstract String startInput();

}

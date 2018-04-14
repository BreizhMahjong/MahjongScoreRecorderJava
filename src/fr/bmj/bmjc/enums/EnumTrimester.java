package fr.bmj.bmjc.enums;

public enum EnumTrimester {

	WINTER {
		@Override
		public String toString() {
			return "hiver";
		}
	},
	SPRING {
		@Override
		public String toString() {
			return "printemps";
		}
	},
	SUMMER {
		@Override
		public String toString() {
			return "été";
		}
	},
	AUTUMN {
		@Override
		public String toString() {
			return "automne";
		}
	}
}

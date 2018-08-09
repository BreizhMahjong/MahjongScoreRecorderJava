package fr.bmj.bmjc.enums;

public enum EnumTrimester {

	TRIMESTER_1 {
		@Override
		public String toString() {
			return "1er";
		}
	},
	TRIMESTER_2 {
		@Override
		public String toString() {
			return "2ème";
		}
	},
	TRIMESTER_3 {
		@Override
		public String toString() {
			return "3ème";
		}
	},
	TRIMESTER_4 {
		@Override
		public String toString() {
			return "4ème";
		}
	}
}

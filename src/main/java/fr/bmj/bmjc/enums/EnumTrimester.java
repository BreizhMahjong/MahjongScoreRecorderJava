package fr.bmj.bmjc.enums;

public enum EnumTrimester {
	TRIMESTER_1("1er"), TRIMESTER_2("2ème"), TRIMESTER_3("3ème"), TRIMESTER_4("4ème");

	private final String display;

	private EnumTrimester(final String display) {
		this.display = display;
	}

	@Override
	public String toString() {
		return display;
	}
}

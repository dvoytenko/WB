package wb.web.app;

import java.util.Comparator;

public class ValueLabel {
	
	public String value;
	
	public String label;
	
	public ValueLabel() {
	}

	public ValueLabel(String value, String label) {
		this.value = value;
		this.label = label;
	}
	
	public static final Comparator<ValueLabel> COMPARATOR_BY_LABEL = new Comparator<ValueLabel>() {
		@Override
		public int compare(ValueLabel v1, ValueLabel v2) {
			return v1.label.compareToIgnoreCase(v2.label);
		}
	};

}

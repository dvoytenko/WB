package wb.model;

public class Voice {
	
	public String id;
	
	public String name;
	
	public String description;
	
	public String gender;
	
	public String language;
	
	public String country;
	
	public String dialect;
	
	public Voice() {
	}

	public Voice(String id, String name, String description, String gender, 
			String language, String country, String dialect) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.gender = gender;
		this.language = language;
		this.country = country;
		this.dialect = dialect;
	}
	
}

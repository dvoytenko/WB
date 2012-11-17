package wb.model;

public class PasteImageEpisode extends ShapeEpisodeBase {
	
	public String imageId;
	
	@Override
	protected Shape resolveShape(PrepareScript preparator) {
		if (this.imageId == null) {
			return null;
		}
		return preparator.getImageShape(this.imageId);
	}

	@Override
	public String toText() {
		return "Paste image: " + this.imageId;
	}

}

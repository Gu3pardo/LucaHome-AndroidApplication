package guepardoapps.lucahome.common;

import java.io.Serializable;

import guepardoapps.toolset.common.Logger;

public class LucaHomeLogger extends Logger implements Serializable {

	private static final long serialVersionUID = -8929350017604753398L;

	public LucaHomeLogger(String tag) {
		super(tag, Constants.DEBUGGING_ENABLED);
	}
}

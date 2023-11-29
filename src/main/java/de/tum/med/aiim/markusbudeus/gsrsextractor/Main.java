package de.tum.med.aiim.markusbudeus.gsrsextractor;

import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {

		GsrsApiClient client = new GsrsApiClient();
		System.out.println(client.findSubstanceByCas("50-78-2"));

	}

}

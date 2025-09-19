package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization.FhirAddress;
import org.hl7.fhir.r4.model.Organization;
import org.neo4j.driver.types.MapAccessorWithDefaultValue;

import java.util.List;

/**
 * @author Markus Budeus
 */
public record GraphOrganization(long mmiId, String name, String shortName, List<GraphAddress> addresses) {

	public static final String MMI_ID = "mmiId";
	public static final String NAME = "name";
	public static final String SHORT_NAME = "shortName";
	public static final String ADDRESSES = "addresses";

	public GraphOrganization(MapAccessorWithDefaultValue value) {
		this(
				value.get(MMI_ID).asLong(),
				value.get(NAME, (String) null),
				value.get(SHORT_NAME, (String) null),
				value.get(ADDRESSES).asList(GraphAddress::new)
		);
	}

	@Deprecated
	public de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization.Organization toLegacyFhirOrganization() {
		de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization.Organization organization
				= new de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization.Organization();
		organization.active = true;
		organization.name = name;
		if (name == null) {
			organization.name = shortName;
		} else if (shortName != null) {
			organization.alias = new String[]{shortName};
		}
		organization.identifier = new Identifier[]{Identifier.fromOrganizationMmiId(mmiId)};
		organization.address = addresses.stream().map(GraphAddress::toFhirAddress).toArray(FhirAddress[]::new);
		return organization;
	}

	public Organization toFhirOrganizaition() {
		Organization organization = new Organization();
		organization.setActive(true);
		organization.setName(name);
		if (name == null) {
			organization.setName(shortName);
		} else if (shortName != null) {
			organization.addAlias(shortName);
		}
		organization.setId("mmi-"+ mmiId);// TODO Put this in a more general place
//		organization.address = addresses.stream().map(GraphAddress::toFhirAddress).toArray(FhirAddress[]::new);
		// TODO add address
		return organization;

	}

}

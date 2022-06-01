package utils;

public abstract class Configuration {
	/* Provides a list of entities */
	public abstract String getBaseUrl();

	/* Provides a list of entities with fields and also notes if CRUD can be done */
	public abstract String getMetadataUrl();

	public abstract String getUsernameAndCompany();

	public abstract String getPp();
}

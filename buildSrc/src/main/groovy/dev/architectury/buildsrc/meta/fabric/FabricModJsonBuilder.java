package dev.architectury.buildsrc.meta.fabric;

import dev.architectury.transformer.shadowed.impl.com.google.gson.JsonObject;

import java.util.Map;

public class FabricModJsonBuilder {
	private int schemaVersion;
	private String id;
	private String version;
	private String name;
	private String description;
	private String[] authors;
	private FabricModJson.ContactInfo contact;
	private String license;
	private String environment;
	private String[] mixins;
	private Map<String, String[]> entrypoints;
	private String icon;
	private Map<String, String> depends;
	private Map<String, String> breaks;
	private JsonObject custom;

	public FabricModJsonBuilder schemaVersion(int schemaVersion) {
		this.schemaVersion = schemaVersion;
		return this;
	}

	public FabricModJsonBuilder id(String id) {
		this.id = id;
		return this;
	}

	public FabricModJsonBuilder version(String version) {
		this.version = version;
		return this;
	}

	public FabricModJsonBuilder name(String name) {
		this.name = name;
		return this;
	}

	public FabricModJsonBuilder description(String description) {
		this.description = description;
		return this;
	}

	public FabricModJsonBuilder authors(String[] authors) {
		this.authors = authors;
		return this;
	}

	public FabricModJsonBuilder contact(FabricModJson.ContactInfo contact) {
		this.contact = contact;
		return this;
	}

	public FabricModJsonBuilder license(String license) {
		this.license = license;
		return this;
	}

	public FabricModJsonBuilder environment(String environment) {
		this.environment = environment;
		return this;
	}

	public FabricModJsonBuilder mixins(String[] mixins) {
		this.mixins = mixins;
		return this;
	}

	public FabricModJsonBuilder entrypoints(Map<String, String[]> entrypoints) {
		this.entrypoints = entrypoints;
		return this;
	}

	public FabricModJsonBuilder icon(String icon) {
		this.icon = icon;
		return this;
	}

	public FabricModJsonBuilder depends(Map<String, String> depends) {
		this.depends = depends;
		return this;
	}

	public FabricModJsonBuilder breaks(Map<String, String> breaks) {
		this.breaks = breaks;
		return this;
	}

	public FabricModJsonBuilder custom(JsonObject custom) {
		this.custom = custom;
		return this;
	}

	public FabricModJson build() {
		return new FabricModJson(schemaVersion, id, version, name, description, authors, contact, license, environment, mixins, entrypoints, icon, depends, breaks, custom);
	}
}
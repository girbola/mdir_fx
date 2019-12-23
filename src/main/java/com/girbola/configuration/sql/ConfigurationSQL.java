package com.girbola.configuration.sql;

public class ConfigurationSQL {
	
	public boolean loadConfig() {
		
		return false;
	}
/*
 * 	if (prop.containsKey(THEMEPATH.getType())) {
			sprintf("ThemePath is: " + prop.getProperty(THEMEPATH.getType()));
			if (!prop.getProperty(THEMEPATH.getType()).isEmpty()) {
				setThemePath(prop.getProperty(THEMEPATH.getType()));
			} else {
				addProperty(THEMEPATH, getThemePath());
			}
		}
		if (prop.containsKey(VLCPATH.getType())) {
			Messages.sprintf("vlcpath: " + getVlcPath());

			if (!prop.getProperty(VLCPATH.getType()).isEmpty()) {
				setVlcPath(prop.getProperty(VLCPATH.getType()));
				Messages.sprintf("vlcpath: " + getVlcPath());
				if (Files.exists(Paths.get(getVlcPath()))) {
					VLCJDiscovery.initVlc();
					VLCJDiscovery.discovery(Paths.get(getVlcPath()));
				} else {
					VLCJDiscovery.discovery(null);
				}

			}
		} else {
			Messages.sprintf("vlcpath: " + getVlcPath());
			addProperty(VLCPATH, getVlcPath());
		}
		if (prop.containsKey(SAVEFOLDER.getType())) {
			if (!prop.getProperty(SAVEFOLDER.getType()).isEmpty()) {
				setSaveFolder(Paths.get(prop.getProperty(SAVEFOLDER.getType())));
			}
		} else {
			addProperty(SAVEFOLDER, getSaveFolder().toString());
		}

		if (prop.containsKey(SHOWHINTS.getType())) {
			if (!prop.getProperty(SHOWHINTS.getType()).isEmpty()) {
				setShowHints(Boolean.parseBoolean(prop.getProperty(SHOWHINTS.getType())));
			}
		} else {
			addProperty(SHOWHINTS, String.valueOf(isShowHints()));
		}
		if (prop.containsKey(SAVETHUMBS.getType())) {
			if (!prop.getProperty(SAVETHUMBS.getType()).isEmpty()) {
				setSavingThumb(Boolean.parseBoolean(prop.getProperty(SAVETHUMBS.getType())));
			}
		} else {
			addProperty(SAVETHUMBS, String.valueOf(isSavingThumb()));
		}
		if (prop.containsKey(CONFIRMONEXIT.getType())) {
			if (!prop.getProperty(CONFIRMONEXIT.getType()).isEmpty()) {
				setConfirmOnExit(Boolean.parseBoolean(prop.getProperty(CONFIRMONEXIT.getType())));
			}
		} else {
			addProperty(CONFIRMONEXIT, String.valueOf(isConfirmOnExit()));
		}

		if (prop.containsKey(SHOWFULLPATH.getType())) {
			if (!prop.getProperty(SHOWFULLPATH.getType()).isEmpty()) {
				setShowFullPath(Boolean.parseBoolean(prop.getProperty(SHOWFULLPATH.getType())));
				sprintf("isShowFullPAth: " + isShowFullPath());
			}
		} else {
			addProperty(SHOWFULLPATH, String.valueOf(isShowFullPath()));
		}
		if (prop.containsKey(SHOWTOOLTIPS.getType())) {
			if (!prop.getProperty(SHOWTOOLTIPS.getType()).isEmpty()) {
				setShowTooltips(Boolean.parseBoolean(prop.getProperty(SHOWTOOLTIPS.getType())));
			}
		} else {
			addProperty(SHOWTOOLTIPS, String.valueOf(isShowTooltips()));
		}
		if (prop.containsKey(BETTERQUALITYTHUMBS.getType())) {
			if (!prop.getProperty(BETTERQUALITYTHUMBS.getType()).isEmpty()) {
				setShowTooltips(Boolean.parseBoolean(prop.getProperty(BETTERQUALITYTHUMBS.getType())));
			}
		} else {
			addProperty(BETTERQUALITYTHUMBS, String.valueOf(isBetterQualityThumbs()));
		}

		if (prop.containsKey(VLCSUPPORT.getType())) {
			if (!prop.getProperty(VLCSUPPORT.getType()).isEmpty()) {
				setVlcSupport(Boolean.parseBoolean(prop.getProperty(VLCSUPPORT.getType())));
			}
		} else {
			addProperty(VLCSUPPORT, String.valueOf(isVlcSupport()));
		}
 */
	
}

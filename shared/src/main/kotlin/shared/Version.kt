package shared

/**
 * Class that represents [semantic versioning](https://semver.org)
 *
 * e.g: 1.0.3 is...
 *
 * 1 - Major
 *
 * 0 - Minor
 *
 * 3 - Fix
 */
data class Version(val major: Int, val minor: Int, val fix: Int) {
    /**
     * Compare versions by separating the version into major, minor and fix.
     *
     * [More here](https://semver.org/#spec-item-11)
     */
    operator fun compareTo(version: Version?): Int {
        if (version == null) {
            return 1 // Always bigger than nothin
        }

        if (major > version.major) {
            return 1
        } else if (major < version.major) {
            return -1
        }

        if (minor > version.minor) {
            return 1
        } else if (minor < version.minor) {
            return -1
        }

        if (fix > version.fix) {
            return 1
        } else if (fix < version.fix) {
            return -1
        }

        return 0 // Both are equal
    }

    override fun toString(): String = "$major.$minor.$fix"
}
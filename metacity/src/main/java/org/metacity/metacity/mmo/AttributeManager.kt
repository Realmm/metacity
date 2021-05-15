package org.metacity.metacity.mmo

/**
 * Manages all Attributes
 */
class AttributeManager {

    private val attributes = mutableListOf<Attribute>()

    /**
     * Register default attributes
     */
    fun addDefaultAttributes() {
        attributes.add(Attribute())
    }

    fun addAttribute(a: Attribute) {
        attributes.add(a);
    }

    fun removeAttribute(a: Attribute) {
        attributes.remove(a);
    }

    fun getAttributes() : List<Attribute> {
        return attributes;
    }

}
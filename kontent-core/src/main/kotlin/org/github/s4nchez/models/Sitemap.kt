package org.github.s4nchez.models

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "urlset")
data class Sitemap(
        @field:Attribute var xmlns: String = "http://www.sitemaps.org/schemas/sitemap/0.9",
        @field:ElementList(inline = true) val urls: List<Url>
)

data class Url(
        @field:Element var loc: String
)
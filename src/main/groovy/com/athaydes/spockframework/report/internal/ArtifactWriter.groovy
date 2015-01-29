/*
 Copyright 2015 Battams, Derek
 
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
 
		http://www.apache.org/licenses/LICENSE-2.0
 
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package com.athaydes.spockframework.report.internal

import groovy.xml.MarkupBuilder

import org.apache.commons.io.FileUtils
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.IterationInfo

class ArtifactWriter {

	static private String getSpecClassName(final def info) {
		def obj = info
		while(obj.parent)
			obj = obj.parent
		return obj.reflection.name
	}

	static private String getIdString(final def leaf, final int itrNum) {
		def ids = []
		def parent = leaf
		while(parent) {
			ids << parent.name
			if(parent instanceof FeatureInfo)
				ids << itrNum
			parent = parent.parent
		}
		ids.join(':').hashCode().toString()
	}
	
	static String getArtifactDir(final def info, final int itrNum) {
		return "${getSpecClassName(info)}/${getIdString(info, itrNum)}"
	}
	
	static private final Map itrCounts = [:]

	private final MarkupBuilder builder
	private final def leaf
	private final File reportBase
	
	ArtifactWriter(MarkupBuilder builder, def leaf, File reportBase) {
		this.builder = builder
		this.leaf = leaf
		if(leaf instanceof IterationInfo) {
			synchronized(ArtifactWriter) {
				def c = itrCounts[leaf.parent]
				if(c == null) {
					c = 1
					itrCounts[leaf.parent] = c
				} else
					itrCounts[leaf.parent] = ++c
			}
		}
		this.reportBase = reportBase
	}
	
	void write() {
		def relDir = getArtifactDir(leaf, (leaf instanceof IterationInfo) ? (itrCounts[leaf.parent] ?: 1) : 1 )
		def artifacts = new File(reportBase, relDir).listFiles()
		if(artifacts?.size() > 0) {
			builder.tr {
				td {
					div('class': 'block-kind', 'Artifacts:')
				}
				artifacts.each { f ->
					if(f.size() > 0) {
						td {
							div {
								span {
									a(href: "$relDir/$f.name", "$f.name (${FileUtils.byteCountToDisplaySize(f.size())})")
								}
							}
						}
					}
				}
			}
		}
	}
}

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

class RolledUpArtifactWriter {

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
	
	private final MarkupBuilder builder
	private final FeatureRun run
	private final File reportBase
	
	RolledUpArtifactWriter(MarkupBuilder builder, FeatureRun run, File reportBase) {
		this.builder = builder
		this.run = run
		this.reportBase = reportBase
	}
	
	void write() {
		def doIt = false
		def keys = run.failuresByIteration.keySet().toArray()
		for(int i = 0; !doIt && i < keys.size(); ++i) {
			def relDir = getArtifactDir(keys[i], i + 1)
			def artifacts = new File(reportBase, relDir)
			doIt = artifacts.listFiles()?.find { it.size() > 0 } != null
		}

		if(doIt) {
			builder.tr {
				td {
					div('class': 'block-kind', 'Artifacts:')
				}
				td(colspan: '10') {
					table('class': 'ex-table') {
						thead()
						tbody {
							run.failuresByIteration.keySet().toArray().eachWithIndex { def info, int i ->
								tr {
									def relDir = getArtifactDir(info, i + 1)
									def artifacts = new File(reportBase, relDir)
									def files = artifacts.listFiles()?.findAll { f -> f.size() > 0 }
									if(files?.size()) {
										files.each { f ->
											td {
												span {
													a(href: "$relDir/$f.name", "$f.name (${FileUtils.byteCountToDisplaySize(f.size())})")
												}
											}
										}
									} else {
										td {
											span('[No artifacts]')
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

#!/usr/bin/env groovy

def pipeline_id = env.BUILD_ID
println "Current pipeline job build id is '${pipeline_id}'"
def node_label = 'CCI && ansible-2.3'

// stage 1: conformance test
stage ('conformance') {
          if (CONFORMANCE) {
                currentBuild.result = "SUCCESS"
                node('CCI && US') {
                        // get properties file
                        if (fileExists("conformance.properties")) {
                                println "Looks like conformance.properties file already exists, erasing it"
                                sh "rm conformance.properties"
                        }
                        // get properties file
                        //sh "wget http://file.rdu.redhat.com/~nelluri/pipeline/conformance.properties"
                        sh "wget ${CONFORMANCE_PROPERTY_FILE}"
                        sh "cat conformance.properties"
                        def conformance_properties = readProperties file: "conformance.properties"
                        def master_hostname = conformance_properties['MASTER_HOSTNAME']
                        def user = conformance_properties['USER']
                        def enable_pbench = conformance_properties['ENABLE_PBENCH']
                        def use_proxy = conformance_properties['USE_PROXY']
                        def proxy_user = conformance_properties['PROXY_USER']
                        def proxy_host = conformance_properties['PROXY_HOST']

                        // debug info
                        println "----------USER DEFINED OPTIONS-------------------"
                        println "-------------------------------------------------"
                        println "-------------------------------------------------"
                        println "JUMP_HOST: '${jump_host}'"
                        println "USER: '${user}'"
                        println "ENABLE_PBENCH: '${enable_pbench}'"
                        println "USE_PROXY: '${use_proxy}'"
                        println "PROXY_USER: '${proxy_user}'"
                        println "PROXY_HOST: '${proxy_host}'"
                        println "-------------------------------------------------"
                        println "------------------"     

                        // Run conformance job
                        try {
                            conformance_build = build job: 'SVT_conformance',
                                parameters: [   [$class: 'LabelParameterValue',  name: 'node', label: node_label ],
                                                [$class: 'StringParameterValue', name: 'MASTER_HOSTNAME', value: master_hostname ],
                                                [$class: 'StringParameterValue', name: 'MASTER_USER', value: user ],
                                                [$class: 'StringParameterValue', name: 'ENABLE_PBENCH', value: enable_pbench ],
                                                [$class: 'StringParameterValue', name: 'USE_PROXY', value: use_proxy ],
                                                [$class: 'StringParameterValue', name: 'PROXY_USER', value: proxy_user ],
                                                [$class: 'StringParameterValue', name: 'PROXY_HOST', value: proxy_host ]]
                        } catch ( Exception e) {
                        echo "CONFORMANCE Job failed with the following error: "
                        echo "${e.getMessage()}"
                        currentBuild.result = "FAILURE"
                        sh "exit 1"
                        }
                        println "CONFORMANCE build ${conformance_build.getNumber()} completed successfully"
                }
        }
                        println "Stage 1: CONFORMANCE OF PIPELINE BUILD '${pipeline_id}' COMPLETED"
}

// stage 2: setup tooling
stage ('setup_pbench') {
	  if (SETUP_TOOLING) {
		currentBuild.result = "SUCCESS"
		node('CCI && US') {
			// get properties file
			if (fileExists("setup_pbench.properties")) {
				println "Looks like setup_pbench.properties file already exists, erasing it"
				sh "rm setup_pbench.properties"
			}
			// get properties file
			//sh "wget http://file.rdu.redhat.com/~nelluri/pipeline/setup_pbench.properties"
			sh "wget ${SETUP_PBENCH_PROPERTY_FILE}"
			sh "cat setup_pbench.properties"
			def pbench_properties = readProperties file: "setup_pbench.properties"
			def jump_host = pbench_properties['JUMP_HOST']
			def user = pbench_properties['USER']
			def tooling_inventory_path = pbench_properties['TOOLING_INVENTORY']
			def openshift_inventory = pbench_properties['OPENSHIFT_INVENTORY']
			// debug info
			println "JUMP_HOST: '${jump_host}'"
			println "USER: '${user}'"
			println "TOOLING_INVENTORY_PATH: '${tooling_inventory_path}'"
			println "OPENSHIFT_INVENTORY_PATH: '${openshift_inventory}'"
		
			// Run setup-tooling job
			try {
			    setup_pbench_build = build job: 'SETUP-TOOLING',
				parameters: [   [$class: 'LabelParameterValue', name: 'node', label: node_label ],
						[$class: 'StringParameterValue', name: 'JUMP_HOST', value: jump_host ],
						[$class: 'StringParameterValue', name: 'USER', value: user ],
						[$class: 'StringParameterValue', name: 'TOOLING_INVENTORY', value: tooling_inventory_path ],
						[$class: 'StringParameterValue', name: 'OPENSHIFT_INVENTORY', value: openshift_inventory ]]
			} catch ( Exception e) {
                	echo "SETUP_TOOLING Job failed with the following error: "
                	echo "${e.getMessage()}"
                	currentBuild.result = "FAILURE"
			sh "exit 1"
            		}
                	println "SETUP_TOOLING build ${setup_pbench_build.getNumber()} completed successfully"
		}
	}
			println "Stage 2: SETUP-TOOLING OF PIPELINE BUILD '${pipeline_id}' COMPLETED"
}

// stage 3: run nodevertical scale test
stage ('nodevertical_scale_test') {
          if (NODEVERTICAL_SCALE_TEST) {
                currentBuild.result = "SUCCESS"
                node('CCI && US') {
                        // get properties file
                        if (fileExists("nodevertical.properties")) {
                                println "Looks like nodevertical.properties file already exists, erasing it"
                                sh "rm nodevertical.properties"
                        }
                        // get properties file
                        //sh "wget http://file.rdu.redhat.com/~nelluri/pipeline/nodevertical.properties"
                        sh "wget ${NODEVERTICAL_PROPERTY_FILE}"
                        sh "cat nodevertical.properties"
			def nodevertical_properties = readProperties file: "nodevertical.properties"
                        def jump_host = nodevertical_properties['JUMP_HOST']
                        def user = nodevertical_properties['USER']
                        def tooling_inventory_path = nodevertical_properties['TOOLING_INVENTORY']
			def clear_results = nodevertical_properties['CLEAR_RESULTS']
			def move_results = nodevertical_properties['MOVE_RESULTS']
			def use_proxy = nodevertical_properties['USE_PROXY']
			def proxy_user = nodevertical_properties['PROXY_USER']
			def proxy_host = nodevertical_properties['PROXY_HOST']
			def containerized = nodevertical_properties['CONTAINERIZED']

                        // debug info
                        println "JUMP_HOST: '${jump_host}'"
                        println "USER: '${user}'"
                        println "TOOLING_INVENTORY_PATH: '${tooling_inventory_path}'"

                        // Run nodevertical job
                        try {
                           nodevertical_build = build job: 'NODEVERTICAL-SCALE-TEST',
                                parameters: [   [$class: 'LabelParameterValue', name: 'node', label: node_label ],
                                                [$class: 'StringParameterValue', name: 'JUMP_HOST', value: jump_host ],
                                                [$class: 'StringParameterValue', name: 'USER', value: user ],
                                                [$class: 'StringParameterValue', name: 'TOOLING_INVENTORY', value: tooling_inventory_path ],
						[$class: 'StringParameterValue', name: 'CLEAR_RESULTS', value: clear_results ],
		                                [$class: 'StringParameterValue', name: 'MOVE_RESULTS', value: move_results ],
						[$class: 'StringParameterValue', name: 'USE_PROXY', value: use_proxy ],
                                                [$class: 'StringParameterValue', name: 'PROXY_USER', value: proxy_user ],
                                                [$class: 'StringParameterValue', name: 'PROXY_HOST', value: proxy_host ],
						[$class: 'StringParameterValue', name: 'CONTAINERIZED', value: containerized ]]
                        } catch ( Exception e) {
                        echo "NODEVERTICAL-SCALE-TEST Job failed with the following error: "
                        echo "${e.getMessage()}"
                        currentBuild.result = "FAILURE"
                        sh "exit 1"
                        }
                        println "NODE-VERTICAL-SCALE-TEST build ${nodevertical_build.getNumber()} completed successfully"
                }
        }
                        println "Stage 3: NODEVERTICAL-SCALE-TEST OF PIPELINE BUILD '${pipeline_id} COMPLETED"
}

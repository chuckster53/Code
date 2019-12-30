/**
 *  Turn on at Sunset
 *
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Turn on at Sunset and check if garage door is open",
    namespace: "chuckster53",
    author: "chuck",
    description: "flash ights at sunset, if garage door is open",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine@2x.png")

preferences {
    section("When any of the following devices trigger..."){
		input "contact", "capability.contactSensor", title: "Contact Sensor?", required: false
    }
   
    section("Lights") {
        input "switches", "capability.switch", title: "Which lights to flash?"
        input "numFlashes", "number", title: "This number of times (default 3)", required: false
    }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(location, "sunset", sunsetHandler)
    if (contact) {
		subscribe(contact, "contact.open", contactOpenHandler)
	}
}

def sunsetHandler(evt) {
    log.debug "turning on lights at sunset"
    flashLights()
}

private flashLights() {
	def doFlash = true
	def onFor = onFor ?: 1000
	def offFor = offFor ?: 1000
	def numFlashes = numFlashes ?: 3

	log.debug "LAST ACTIVATED IS: ${state.lastActivated}"
	if (state.lastActivated) {
		def elapsed = now() - state.lastActivated
		def sequenceTime = (numFlashes + 1) * (onFor + offFor)
		doFlash = elapsed > sequenceTime
		log.debug "DO FLASH: $doFlash, ELAPSED: $elapsed, LAST ACTIVATED: ${state.lastActivated}"
	}

	if (doFlash) {
		log.debug "FLASHING $numFlashes times"
		state.lastActivated = now()
		log.debug "LAST ACTIVATED SET TO: ${state.lastActivated}"
		def initialActionOn = switches.collect{it.currentSwitch != "on"}
		def delay = 0L
		numFlashes.times {
			log.trace "Switch on after  $delay msec"
			switches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.on(delay: delay)
				}
				else {
					s.off(delay:delay)
				}
			}
			delay += onFor
			log.trace "Switch off after $delay msec"
			switches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.off(delay: delay)
				}
				else {
					s.on(delay:delay)
				}
			}
			delay += offFor
		}
	}
}

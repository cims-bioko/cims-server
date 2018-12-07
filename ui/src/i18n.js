import Vue from 'vue'
import VueI18n from 'vue-i18n'

Vue.use(VueI18n)

function loadLocaleMessages() {
    const locales = require.context('./locales', false, /[A-Za-z0-9-_,\s]+\.json$/i)
    const messages = {}
    locales.keys().forEach(key => {
        const matched = key.match(/([A-Za-z0-9-_]+)\./i)
        if (matched && matched.length > 1) {
            const locale = matched[1]
            messages[locale] = locales(key)
        }
    })
    return messages
}

const messages = loadLocaleMessages()
const supportedLocales = Object.keys(messages)
const normalizedSupportedLocales = supportedLocales.map(normalize)
const fallbackLocale = 'en'

/**
 * Generates candidate locales for the specified locale. The ordering conforms to the Java ResourceBundle selection
 * algorithm. For example, 'en-US-UNIX' becomes ['en-US-UNIX', 'en-US', 'en']. Clients can then filter the
 * list of candidates to available locales and select the first, yielding the most specific match for the originally
 * specified locale.
 *
 * @param l the preferred locale, for example 'en-US-UNIX'
 * @returns {*} the list of candidates, for example ['en-US-UNIX', 'en-US', 'en']
 */
function candidates(l) {

    if (!l) { return [] }

    if (typeof l === 'string') { return candidates(l.split('-')) }

    if (!l.length || l.length <= 0) { return [] }

    if (l.length === 1) { return l }

    return [l.join('-')].concat(candidates(l.slice(0, l.length-1)))
}

function normalize(l) {
    return l.toLowerCase()
}

function findSupportedLocale(preference) {
    return candidates(preference).filter(p => normalizedSupportedLocales.includes(normalize(p)))[0] || fallbackLocale
}

function findLocaleForBrowser() {
    return findSupportedLocale(navigator.language)
}

export const locale = findLocaleForBrowser()

const dateTimeFormats = {
    'en': {
        short: {
            year: 'numeric', month: 'short', day: 'numeric'
        },
        long: {
            year: 'numeric', month: 'short', day: 'numeric', hour: 'numeric', minute: 'numeric'
        }
    }
}

export const i18n = new VueI18n({
    messages,
    locale,
    fallbackLocale,
    dateTimeFormats
})

/* update locale when the user swaps browser language */
window.onlanguagechange = () => { i18n.locale = findLocaleForBrowser() }

export default i18n

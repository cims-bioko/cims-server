import i18n from "@/i18n"

function toDateObj(v) {
    return typeof v === 'string'? new Date(v) : v
}

export function formatDate(v) {
    if (v) {
        return i18n.d(toDateObj(v), 'short')
    }
}

export function formatDateTime(v) {
    if (v) {
        return i18n.d(toDateObj(v), 'long')
    }
}

export function formatLargeNumber(v) {
    if (v) {
        const a = ['', 'K', 'M', 'B', 't', 'q', 'Q']
        let i = 0
        while (v / 1000 > 1) {
            i += 1
            v /= 1000
        }
        const metric = i > 0 && i < a.length? i18n.t(`metrics.${a[i]}`) : ""
        return `${v.toFixed(2)}${metric}`
    }
}
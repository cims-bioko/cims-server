export function isValidDate(date) {
    return date && date.getTime && !isNaN(date.getTime())
}
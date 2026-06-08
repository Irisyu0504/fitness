export async function request(path, options = {}) {
    const token = localStorage.getItem('token')

    const response = await fetch(`/api${path}`, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
            ...options.headers
        }
    })

    const body = await response.json()

    if (!response.ok || Number(body.code) !== 200) {
        throw new Error(body.msg || '请求失败')
    }

    return body.data
}

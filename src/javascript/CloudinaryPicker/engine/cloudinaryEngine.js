

export async function postData(path = '', data = {}) {
    const config = window.contextJsParameters.config.cloudinary;
    const basicCredential = btoa(`${config.apiKey}:${config.apiSecret}`);
    const baseUrl = `${config.apiSchema}://${config.apiEndPoint}/${config.apiVersion}/${config.cloudName}`;
    // Default options are marked with *
    const response = await fetch(baseUrl+path, {
        method: 'POST', // *GET, POST, PUT, DELETE, etc.
        //mode: 'no-cors', // no-cors, *cors, same-origin
        // cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
        // credentials: 'same-origin', // include, *same-origin, omit
        headers: {
            'Content-Type': 'application/json',
            Authorization : `Basic ${basicCredential}`
        },
        // redirect: 'follow', // manual, *follow, error
        // referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
        body: JSON.stringify(data) // body data type must match "Content-Type" header
    });
    return response.json(); // parses JSON response into native JavaScript objects
}

package com.bikeprojectminji.bikefront.address

interface AddressSearchGateway {

    fun search(
        query: String,
        accessToken: String,
        onSuccess: (AddressSearchUiModel) -> Unit,
        onFailure: (String) -> Unit,
    )
}


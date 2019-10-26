package com.passangers.junctionx.backend.endpoint

import com.passangers.junctionx.backend.model.Atm
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

@RestController
class AtmController {

    @GetMapping("atm")
    fun getAtms(
        @RequestParam("location", required = false)
        location: String?,
        @RequestParam("ne", required = true)
        ne: String,
        @RequestParam("sw", required = true)
        sw: String,
        @RequestParam("canDeposit", required = false)
        canDeposit: Boolean
    ): ResponseEntity<GetAtmResponse> {
        val itemsMap = HashMap<String, Atm>()
        File("/Users/avanisimov/Downloads/atm_201909_IX_eng.txt")
            .forEachLine(Charset.forName("windows-1252")) { line ->
                val dataArray = line.split('\t')
                if (dataArray[4] != "STREET_ADDRESS") {
                    itemsMap.put(dataArray[4],
                        Atm(
                            id = UUID.randomUUID(),
                            city = dataArray[3],
                            zipCD = dataArray[2],
                            address = dataArray[4],
                            geoX = dataArray[5].replace(',', '.').toDouble(),
                            geoY = dataArray[6].replace(',', '.').toDouble(),
                            canDeposit = dataArray[1] == "Y"
                        )
                    )
                }
            }

        val items = itemsMap.values.toList();
        return ResponseEntity.ok(
            GetAtmResponse(
                items.size,
                items
            )
        )
    }
}

data class GetAtmResponse(
    val total: Int,
    val items: List<Atm>
)


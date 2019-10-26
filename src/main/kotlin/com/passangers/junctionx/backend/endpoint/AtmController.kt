package com.passangers.junctionx.backend.endpoint

import com.passangers.junctionx.backend.model.Atm
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.HashMap

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
        canDeposit: Boolean?
    ): ResponseEntity<GetAtmResponse> {
        val itemsMap = HashMap<String, Atm>()
        File("/Users/avanisimov/Downloads/atm_201909_IX_eng.txt")
            .forEachLine(Charset.forName("windows-1252")) { line ->
                val dataArray = line.split('\t')
                if (dataArray[4] != "STREET_ADDRESS") {
                    itemsMap.put(
                        dataArray[4],
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

        val items = itemsMap.values.toList()

        val nord = ne.split(',')[0].toDouble()
        val east = ne.split(',')[1].toDouble()
        val south = sw.split(',')[0].toDouble()
        val west = sw.split(',')[1].toDouble()

        val filteredItems = items.filter {
            if (it.geoX < south
                || it.geoX > nord
                || it.geoY < west
                || it.geoY > east
            ) {
                return@filter false
            }
            if (canDeposit != null) {
                if (it.canDeposit != canDeposit) {
                    return@filter false
                }
            }
            return@filter true
        }
        return ResponseEntity.ok(
            GetAtmResponse(
                filteredItems.size,
                filteredItems
            )
        )
    }
}

data class GetAtmResponse(
    val total: Int,
    val items: List<Atm>
)


package com.example.gestionenegozio.dati.database

import androidx.room.TypeConverter
import com.example.gestionenegozio.dati.entita.RuoloUtente
import com.example.gestionenegozio.dati.entita.MetodoPagamento

class Convertitori {

    @TypeConverter
    fun daRuoloUtente(valore: RuoloUtente): String {
        return valore.name
    }

    @TypeConverter
    fun aRuoloUtente(valore: String): RuoloUtente {
        return RuoloUtente.valueOf(valore)
    }

    @TypeConverter
    fun daMetodoPagamento(valore: MetodoPagamento): String {
        return valore.name
    }

    @TypeConverter
    fun aMetodoPagamento(valore: String): MetodoPagamento {
        return MetodoPagamento.valueOf(valore)
    }
}
package com.example.data.model

import java.util.UUID

data class LeadDraft(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String,
    val email: String = "",
    val location: String = "",
    val company: String = "",
    val notes: String = ""
) {
    fun toContactLead(campaignId: Long): ContactLead {
        return ContactLead(
            campaignId = campaignId,
            name = name.ifBlank { "Contact Lead" },
            phoneNumber = phoneNumber,
            email = email,
            location = location,
            company = company,
            notes = notes,
            status = "PENDING"
        )
    }
}

object LeadParser {
    /**
     * Parses multiple lines of text formatted as CSV, TSV, semicolon or pipe-separated values.
     * Expects rows such as:
     * Name, Number, Email, Location
     * or
     * Name, Number
     */
    fun parseText(rawText: String): List<LeadDraft> {
        val results = mutableListOf<LeadDraft>()
        val lines = rawText.lines()

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isBlank()) continue

            val delimiter = when {
                line.contains("\t") -> "\t"
                line.contains(",") -> ","
                line.contains(";") -> ";"
                line.contains("|") -> "|"
                else -> ","
            }

            val tokens = line.split(delimiter).map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
            if (tokens.isEmpty()) continue

            // Detect header row
            val first = tokens[0].lowercase()
            val hasPhoneHeader = tokens.any { it.lowercase() in listOf("phone", "phone number", "mobile", "cell", "number", "tel") }
            val hasNameHeader = first in listOf("name", "full name", "contact", "lead name", "first name", "client")
            if (hasNameHeader || hasPhoneHeader) {
                continue // skip header row
            }

            var name = ""
            var phone = ""
            var email = ""
            var location = ""
            var company = ""
            var notes = ""

            when (tokens.size) {
                1 -> {
                    val token = tokens[0]
                    if (token.any { it.isDigit() }) {
                        name = "Lead ${results.size + 1}"
                        phone = token
                    } else {
                        name = token
                        phone = ""
                    }
                }
                2 -> {
                    // Check which one is phone
                    if (tokens[0].any { it.isDigit() } && !tokens[1].any { it.isDigit() }) {
                        phone = tokens[0]
                        name = tokens[1]
                    } else {
                        name = tokens[0]
                        phone = tokens[1]
                    }
                }
                3 -> {
                    name = tokens[0]
                    phone = tokens[1]
                    if (tokens[2].contains("@")) {
                        email = tokens[2]
                    } else {
                        location = tokens[2]
                    }
                }
                else -> {
                    name = tokens[0]
                    phone = tokens[1]
                    // Token 2
                    if (tokens[2].contains("@")) {
                        email = tokens[2]
                        location = tokens[3]
                    } else {
                        location = tokens[2]
                        email = if (tokens[3].contains("@")) tokens[3] else ""
                    }
                    if (tokens.size > 4) {
                        company = tokens[4]
                    }
                    if (tokens.size > 5) {
                        notes = tokens.drop(5).joinToString(", ")
                    }
                }
            }

            if (name.isNotBlank() || phone.isNotBlank()) {
                results.add(
                    LeadDraft(
                        name = if (name.isBlank()) "Lead ${results.size + 1}" else name,
                        phoneNumber = phone,
                        email = email,
                        location = location,
                        company = company,
                        notes = notes
                    )
                )
            }
        }

        return results
    }
}

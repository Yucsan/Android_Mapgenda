package com.yucsan.mapgendafernandochang2025.util

// Define las categorías agrupadas por tipo
val categoriasPorGrupo = linkedMapOf(
    "Comida" to listOf("restaurant", "cafe", "bakery", "supermarket", "bar","food","meal_delivery","meal_takeaway"),
    "Compras" to listOf("bicycle_store", "pet_store", "electronics_store", "hardware_store","clothing_store","shoe_store","furniture_store","jewelry_store","liquor_store","department_store","shopping_mall","convenience_store","atm","car_wash","car_rental"),
    "Cultura" to listOf("library", "mosque", "university", "church", "primary_school","hindu_temple", "synagogue","museum","art_gallery"),
    "Hospedaje" to listOf("lodging","hotel","hostel","motel","campground","guest_house","bed_and_breakfast"),
    "Aire Libre" to listOf("park", "rv_park","beach","natural_feature","hiking_trail","camp_site"),
    "Ocio" to listOf("amusement_park", "zoo", "gym", "spa", "bowling_alley","stadium","movie_theater","night_club","casino","rv_park"),
    "Transporte" to listOf("gas_station", "bus_station", "subway_station", "train_station","airport","taxi_stand","car_rental","parking"),
    "Govierno" to listOf("city_hall", "courthouse", "embassy","fire_station","post_office"),
    "Salud" to listOf("pharmacy", "doctor", "hospital", "dentist", "veterinary_care"),
    "Custom" to listOf("custom")

)

// También si quieres rápido acceso a la lista total
val todasLasCategorias: List<String> = categoriasPorGrupo.values.flatten().distinct()

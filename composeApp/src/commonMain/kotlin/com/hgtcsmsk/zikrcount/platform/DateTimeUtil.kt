package com.hgtcsmsk.zikrcount.platform

/**
 * Bir zaman damgasını (timestamp) alır ve onu platforma özgü,
 * cihazın dil/bölge ayarlarına uygun (örn: 18.10.2025 veya 10/18/2025)
 * kısa bir tarih/saat metnine dönüştürür.
 *
 * @param timestamp Milisaniye cinsinden epoch zaman damgası.
 * @param unknownDateText Hata durumunda gösterilecek yerelleştirilmiş metin.
 * @return Formatlanmış tarih/saat metni.
 */
expect fun formatTimestampToLocalDateTime(timestamp: Long, unknownDateText: String): String
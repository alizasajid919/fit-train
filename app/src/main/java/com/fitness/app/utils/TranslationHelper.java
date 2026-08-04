package com.fitness.app.utils;

import java.util.HashMap;
import java.util.Map;

public class TranslationHelper {
    private static final Map<String, Map<String, String>> dictionary = new HashMap<>();

    static {
        // Spanish Translations
        Map<String, String> es = new HashMap<>();
        es.put("settings", "Ajustes");
        es.put("account", "Cuenta");
        es.put("edit personal details", "Editar Detalles Personales");
        es.put("preferences", "Preferencias");
        es.put("language", "Idioma");
        es.put("select language", "Seleccionar idioma");
        es.put("dark mode", "Modo Oscuro");
        es.put("metric units (kg/cm)", "Unidades Métricas (kg/cm)");
        es.put("no body-shaming & support mode", "Modo de Apoyo Sin Críticas");
        es.put("sync data to cloud now", "Sincronizar Datos en la Nube");
        es.put("cloud sync & backup", "Sincronización en la Nube");
        es.put("sync status: local only (guest user)", "Sincronización: Solo Local (Invitado)");
        es.put("sync status: synchronized", "Sincronización: Sincronizado");
        es.put("last sync: never", "Última Sincronización: Nunca");
        es.put("terms & conditions", "Términos y Condiciones");
        es.put("about fittrain", "Acerca de FitTrain");
        es.put("log out", "Cerrar Sesión");
        es.put("about", "Acerca de");
        es.put("hello, guest user", "Hola, Usuario Invitado");
        es.put("hello,", "Hola,");
        es.put("goal:", "Objetivo:");
        es.put("level", "Nivel");
        es.put("today's ai fitness task", "TAREA DE FITNESS AI DE HOY");
        es.put("in progress", "En Progreso");
        es.put("completed ✓", "Completado ✓");
        es.put("workout: loading plan...", "Entrenamiento: Cargando plan...");
        es.put("diet: loading plan...", "Dieta: Cargando plan...");
        es.put("complete today's tasks", "Completar Tareas de Hoy");
        es.put("completed for today! 🎉", "¡Completado por hoy! 🎉");
        es.put("today's activity", "Actividad de Hoy");
        es.put("steps", "Pasos");
        es.put("step goal achieved! 🎉", "¡Objetivo de pasos logrado! 🎉");
        es.put("calories", "Calorías");
        es.put("water intake", "Consumo de Agua");
        es.put("water goal achieved! 💧", "¡Objetivo de agua logrado! 💧");
        es.put("sleep", "Sueño");
        es.put("sleep goal achieved! 😴", "¡Objetivo de sueño logrado! 😴");
        es.put("body mass index", "Índice de Masa Corporal");
        es.put("log weight/height to calculate bmi", "Registra peso/altura para calcular el IMC");
        es.put("ai workout generator", "Generador de Entrenamientos AI");
        es.put("ai fitness challenges", "Desafíos de Fitness AI");
        es.put("featured workout", "Entrenamiento Destacado");
        es.put("full body shred", "Destrucción de Cuerpo Completo");
        es.put("start workout", "Comenzar Entrenamiento");
        es.put("ai coach", "Entrenador AI");
        es.put("chat with ai", "Chatea con AI");
        es.put("ai smart squat analyzer", "Analizador de Sentadillas AI");
        es.put("view history", "Ver Historial");
        es.put("start analyzer", "Iniciar Analizador");
        es.put("ai recommendations", "Recomendaciones AI");
        es.put("transformation program", "Programa de Transformación");
        es.put("log workout", "Registrar Entrenamiento");
        es.put("log metrics", "Registrar Métricas");
        es.put("select equipment", "Seleccionar Equipo");
        es.put("no equipment", "Sin Equipo");
        es.put("dumbbells", "Mancuernas");
        es.put("barbell", "Barra");
        es.put("kettlebell", "Pesa Rusa");
        es.put("resistance band", "Banda de Resistencia");
        es.put("pull-up bar", "Barra de Dominadas");
        es.put("generate ai workout", "Generar Entrenamiento AI");
        es.put("generating...", "Generando...");
        es.put("active", "Activo");
        es.put("all", "Todo");
        es.put("weekly", "Semanal");
        es.put("monthly", "Mensual");
        es.put("completed", "Completado");
        es.put("upcoming", "Próximo");
        es.put("rules & benefits", "Reglas y Beneficios");
        es.put("milestone celebration", "Celebración de Hito");
        es.put("ai fitness coach", "Entrenador de Fitness AI");
        es.put("ask ai coach", "Preguntar al Entrenador AI");
        es.put("type a message...", "Escribe un mensaje...");
        es.put("send", "Enviar");
        es.put("clear chat", "Limpiar Chat");
        es.put("profile", "Perfil");
        es.put("edit profile", "Editar Perfil");
        es.put("personal stats", "Estadísticas Personales");
        es.put("xp earned", "XP Ganados");
        es.put("challenges completed", "Desafíos Completados");
        es.put("badges unlocked", "Insignias Desbloqueadas");
        es.put("meal planner", "Planificador de Comidas");
        es.put("today's meals", "Comidas de Hoy");
        es.put("breakfast", "Desayuno");
        es.put("lunch", "Almuerzo");
        es.put("dinner", "Cena");
        es.put("snacks", "Aperitivos");
        es.put("add meal", "Añadir Comida");
        es.put("diet type", "Tipo de Dieta");
        es.put("target calories", "Calorías Objetivo");
        es.put("cancel", "Cancelar");
        es.put("save", "Guardar");
        es.put("back", "Volver");
        es.put("next", "Siguiente");
        es.put("confirm", "Confirmar");
        es.put("error", "Error");
        es.put("success", "Éxito");
        es.put("please fill all fields", "Por favor complete todos los campos");
        es.put("profile updated successfully", "Perfil actualizado con éxito");
        es.put("syncing...", "Sincronizando...");
        es.put("saved successfully", "Guardado con éxito");
        es.put("weight", "Peso");
        es.put("height", "Altura");
        es.put("activity level", "Nivel de Actividad");
        es.put("fitness goal", "Objetivo de Fitness");
        es.put("age", "Edad");
        es.put("gender", "Género");
        es.put("male", "Masculino");
        es.put("female", "Femenino");
        es.put("date of birth", "Fecha de Nacimiento");
        es.put("email address", "Correo electrónico");
        es.put("phone number", "Número de teléfono");
        es.put("full name", "Nombre Completo");
        dictionary.put("es", es);

        // German Translations
        Map<String, String> de = new HashMap<>();
        de.put("settings", "Einstellungen");
        de.put("account", "Konto");
        de.put("edit personal details", "Persönliche Daten bearbeiten");
        de.put("preferences", "Präferenzen");
        de.put("language", "Sprache");
        de.put("select language", "Sprache auswählen");
        de.put("dark mode", "Dunkelmodus");
        de.put("metric units (kg/cm)", "Metrische Einheiten (kg/cm)");
        de.put("no body-shaming & support mode", "Unterstützungsmodus");
        de.put("sync data to cloud now", "Daten jetzt in Cloud synchronisieren");
        de.put("cloud sync & backup", "Cloud-Synchronisierung");
        de.put("sync status: local only (guest user)", "Synchronisierung: Nur Lokal (Gast)");
        de.put("sync status: synchronized", "Synchronisierung: Synchronisiert");
        de.put("last sync: never", "Letzte Synchronisierung: Nie");
        de.put("terms & conditions", "Allgemeine Geschäftsbedingungen");
        de.put("about fittrain", "Über FitTrain");
        de.put("log out", "Abmelden");
        de.put("about", "Über");
        de.put("hello, guest user", "Hallo, Gastbenutzer");
        de.put("hello,", "Hallo,");
        de.put("goal:", "Ziel:");
        de.put("level", "Stufe");
        de.put("today's ai fitness task", "HEUTIGE AI-FITNESS-AUFGABE");
        de.put("in progress", "In Bearbeitung");
        de.put("completed ✓", "Abgeschlossen ✓");
        de.put("workout: loading plan...", "Training: Lade Plan...");
        de.put("diet: loading plan...", "Diät: Lade Plan...");
        de.put("complete today's tasks", "Heutige Aufgaben abschließen");
        de.put("completed for today! 🎉", "Für heute abgeschlossen! 🎉");
        de.put("today's activity", "Heutige Aktivität");
        de.put("steps", "Schritte");
        de.put("step goal achieved! 🎉", "Schrittziel erreicht! 🎉");
        de.put("calories", "Kalorien");
        de.put("water intake", "Wasseraufnahme");
        de.put("water goal achieved! 💧", "Wasserziel erreicht! 💧");
        de.put("sleep", "Schlaf");
        de.put("sleep goal achieved! 😴", "Schlafziel erreicht! 😴");
        de.put("body mass index", "Body-Mass-Index");
        de.put("log weight/height to calculate bmi", "Gewicht/Größe eingeben, um BMI zu berechnen");
        de.put("ai workout generator", "AI-Trainingsgenerator");
        de.put("ai fitness challenges", "AI-Fitness-Herausforderungen");
        de.put("featured workout", "Ausgewähltes Training");
        de.put("full body shred", "Ganzkörper-Shred");
        de.put("start workout", "Training starten");
        de.put("ai coach", "AI-Coach");
        de.put("chat with ai", "Mit AI chatten");
        de.put("ai smart squat analyzer", "AI Smart Squat Analyzer");
        de.put("view history", "Verlauf anzeigen");
        de.put("start analyzer", "Analyzer starten");
        de.put("ai recommendations", "AI-Empfehlungen");
        de.put("transformation program", "Transformationsprogramm");
        de.put("log workout", "Training loggen");
        de.put("log metrics", "Metriken loggen");
        de.put("select equipment", "Ausrüstung auswählen");
        de.put("no equipment", "Keine Ausrüstung");
        de.put("dumbbells", "Kurzhanteln");
        de.put("barbell", "Langhantel");
        de.put("kettlebell", "Kettlebell");
        de.put("resistance band", "Widerstandsband");
        de.put("pull-up bar", "Klimmzugstange");
        de.put("generate ai workout", "AI-Training generieren");
        de.put("generating...", "Generiere...");
        de.put("active", "Aktiv");
        de.put("all", "Alle");
        de.put("weekly", "Wöchentlich");
        de.put("monthly", "Monatlich");
        de.put("completed", "Abgeschlossen");
        de.put("upcoming", "Bevorstehend");
        de.put("rules & benefits", "Regeln und Vorteile");
        de.put("milestone celebration", "Meilenstein-Feier");
        de.put("ai fitness coach", "AI-Fitness-Coach");
        de.put("ask ai coach", "AI-Coach fragen");
        de.put("type a message...", "Schreibe eine Nachricht...");
        de.put("send", "Senden");
        de.put("clear chat", "Chat leeren");
        de.put("profile", "Profil");
        de.put("edit profile", "Profil bearbeiten");
        de.put("personal stats", "Persönliche Statistiken");
        de.put("xp earned", "XP verdient");
        de.put("challenges completed", "Herausforderungen abgeschlossen");
        de.put("badges unlocked", "Abzeichen freigeschaltet");
        de.put("meal planner", "Mahlzeitenplaner");
        de.put("today's meals", "Heutige Mahlzeiten");
        de.put("breakfast", "Frühstück");
        de.put("lunch", "Mittagessen");
        de.put("dinner", "Abendessen");
        de.put("snacks", "Snacks");
        de.put("add meal", "Mahlzeit hinzufügen");
        de.put("diet type", "Diät-Typ");
        de.put("target calories", "Zielkalorien");
        de.put("cancel", "Abbrechen");
        de.put("save", "Speichern");
        de.put("back", "Zurück");
        de.put("next", "Weiter");
        de.put("confirm", "Bestätigen");
        de.put("error", "Fehler");
        de.put("success", "Erfolgreich");
        de.put("please fill all fields", "Bitte füllen Sie alle Felder aus");
        de.put("profile updated successfully", "Profil erfolgreich aktualisiert");
        de.put("syncing...", "Synchronisiere...");
        de.put("saved successfully", "Erfolgreich gespeichert");
        de.put("weight", "Gewicht");
        de.put("height", "Größe");
        de.put("activity level", "Aktivitätslevel");
        de.put("fitness goal", "Fitnessziel");
        de.put("age", "Alter");
        de.put("gender", "Geschlecht");
        de.put("male", "Männlich");
        de.put("female", "Weiblich");
        de.put("date of birth", "Geburtsdatum");
        de.put("email address", "E-Mail-Adresse");
        de.put("phone number", "Telefonnummer");
        de.put("full name", "Vollständiger Name");
        dictionary.put("de", de);

        // Urdu Translations
        Map<String, String> ur = new HashMap<>();
        ur.put("settings", "ترتیبات");
        ur.put("account", "اکاؤنٹ");
        ur.put("edit personal details", "ذاتی تفصیلات کی ترمیم");
        ur.put("preferences", "ترجیحات");
        ur.put("language", "زبان");
        ur.put("select language", "زبان منتخب کریں");
        ur.put("dark mode", "ڈارک موڈ");
        ur.put("metric units (kg/cm)", "میٹرک یونٹس (کلوگرام/سینٹی میٹر)");
        ur.put("no body-shaming & support mode", "سپورٹ موڈ (باڈی شیمنگ کے بغیر)");
        ur.put("sync data to cloud now", "کلاؤڈ پر ڈیٹا سنک کریں");
        ur.put("cloud sync & backup", "کلاؤڈ مطابقت پذیری");
        ur.put("sync status: local only (guest user)", "سنک سٹیٹس: صرف مقامی (مہمان صارف)");
        ur.put("sync status: synchronized", "سنک سٹیٹس: ہم آہنگ");
        ur.put("last sync: never", "آخری مطابقت پذیری: کبھی نہیں");
        ur.put("terms & conditions", "شرائط و ضوابط");
        ur.put("about fittrain", "FitTrain کے بارے میں");
        ur.put("log out", "لاگ آؤٹ");
        ur.put("about", "کے بارے میں");
        ur.put("hello, guest user", "ہیلو، مہمان صارف");
        ur.put("hello,", "ہیلو،");
        ur.put("goal:", "مقصد:");
        ur.put("level", "لیول");
        ur.put("today's ai fitness task", "آج کا اے آئی فٹنس ٹاسک");
        ur.put("in progress", "جاری ہے");
        ur.put("completed ✓", "مکمل ✓");
        ur.put("workout: loading plan...", "ورزش: پلان لوڈ ہو رہا ہے...");
        ur.put("diet: loading plan...", "ڈائیٹ: پلان لوڈ ہو رہا ہے...");
        ur.put("complete today's tasks", "آج کے کام مکمل کریں");
        ur.put("completed for today! 🎉", "آج کے لیے مکمل! 🎉");
        ur.put("today's activity", "آج کی سرگرمی");
        ur.put("steps", "قدم");
        ur.put("step goal achieved! 🎉", "قدموں کا ہدف حاصل کر لیا گیا! 🎉");
        ur.put("calories", "کیلوریز");
        ur.put("water intake", "پانی کا استعمال");
        ur.put("water goal achieved! 💧", "پانی کا ہدف حاصل کر لیا گیا! 💧");
        ur.put("sleep", "نیند");
        ur.put("sleep goal achieved! 😴", "نیند کا ہدف حاصل کر لیا گیا! 😴");
        ur.put("body mass index", "باڈی ماس انڈیکس");
        ur.put("log weight/height to calculate bmi", "BMI کے لیے وزن/قد درج کریں");
        ur.put("ai workout generator", "اے آئی ورزش بنانے والا");
        ur.put("ai fitness challenges", "اے آئی فٹنس چیلنجز");
        ur.put("featured workout", "نمایاں ورزش");
        ur.put("full body shred", "فل باڈی شریڈ");
        ur.put("start workout", "ورزش شروع کریں");
        ur.put("ai coach", "اے آئی کوچ");
        ur.put("chat with ai", "اے آئی سے چیٹ کریں");
        ur.put("ai smart squat analyzer", "اے آئی سمارٹ اسکواٹ اینالائزر");
        ur.put("view history", "تاریخچہ دیکھیں");
        ur.put("start analyzer", "تجزیہ شروع کریں");
        ur.put("ai recommendations", "اے آئی سفارشات");
        ur.put("transformation program", "تبدیلی کا پروگرام");
        ur.put("log workout", "ورزش لاگ کریں");
        ur.put("log metrics", "میٹرکس لاگ کریں");
        ur.put("select equipment", "سامان منتخب کریں");
        ur.put("no equipment", "کوئی سامان نہیں");
        ur.put("dumbbells", "ڈمبلز");
        ur.put("barbell", "باربل");
        ur.put("kettlebell", "کیٹل بیل");
        ur.put("resistance band", "مزاحمتی بینڈ");
        ur.put("pull-up bar", "پل اپ بار");
        ur.put("generate ai workout", "ورزش تیار کریں");
        ur.put("generating...", "تیار ہو رہا ہے...");
        ur.put("active", "فعال");
        ur.put("all", "تمام");
        ur.put("weekly", "ہفتہ وار");
        ur.put("monthly", "ماہانہ");
        ur.put("completed", "مکمل");
        ur.put("upcoming", "آنے والا");
        ur.put("rules & benefits", "قوانین اور فوائد");
        ur.put("milestone celebration", "سنگ میل کا جشن");
        ur.put("ai fitness coach", "اے آئی فٹنس کوچ");
        ur.put("ask ai coach", "اے آئی کوچ سے پوچھیں");
        ur.put("type a message...", "پیغام لکھیں...");
        ur.put("send", "بھیجیں");
        ur.put("clear chat", "چیٹ صاف کریں");
        ur.put("profile", "پروفائل");
        ur.put("edit profile", "پروفائل ایڈٹ کریں");
        ur.put("personal stats", "ذاتی اعدادوشمار");
        ur.put("xp earned", "حاصل کردہ XP");
        ur.put("challenges completed", "مکمل کردہ چیلنجز");
        ur.put("badges unlocked", "انلاک شدہ بیجز");
        ur.put("meal planner", "کھانے کا منصوبہ ساز");
        ur.put("today's meals", "آج کا کھانا");
        ur.put("breakfast", "ناشتہ");
        ur.put("lunch", "دوپہر کا کھانا");
        ur.put("dinner", "رات کا کھانا");
        ur.put("snacks", "سنیکس");
        ur.put("add meal", "کھانا شامل کریں");
        ur.put("diet type", "ڈائیٹ کی قسم");
        ur.put("target calories", "ٹارگٹ کیلوریز");
        ur.put("cancel", "منسوخ");
        ur.put("save", "محفوظ کریں");
        ur.put("back", "واپس");
        ur.put("next", "اگلا");
        ur.put("confirm", "تصدیق");
        ur.put("error", "غلطی");
        ur.put("success", "کامیابی");
        ur.put("please fill all fields", "براہ کرم تمام خانے پُر کریں");
        ur.put("profile updated successfully", "پروفائل کامیابی سے اپ ڈیٹ ہو گئی");
        ur.put("syncing...", "مطابقت پذیر ہو رہا ہے...");
        ur.put("saved successfully", "کامیابی سے محفوظ ہو گیا");
        ur.put("weight", "وزن");
        ur.put("height", "قد");
        ur.put("activity level", "سرگرمی کی سطح");
        ur.put("fitness goal", "فٹنس کا ہدف");
        ur.put("age", "عمر");
        ur.put("gender", "جنس");
        ur.put("male", "مرد");
        ur.put("female", "عورت");
        ur.put("date of birth", "تاریخ پیدائش");
        ur.put("email address", "ای میل ایڈریس");
        ur.put("phone number", "فون نمبر");
        ur.put("full name", "پورا نام");
        dictionary.put("ur", ur);
    }

    public static String getTranslation(String text, String langCode) {
        if (text == null || text.trim().isEmpty()) return text;
        if (langCode == null || langCode.trim().isEmpty() || langCode.toLowerCase().startsWith("en")) {
            return text;
        }

        String cleanedLang = langCode.trim().toLowerCase();
        if (cleanedLang.length() > 2) {
            cleanedLang = cleanedLang.substring(0, 2);
        }

        Map<String, String> langMap = dictionary.get(cleanedLang);
        if (langMap == null) return text;

        String key = text.trim().toLowerCase();

        // Handle dynamic patterns like "Goal: Lose Weight"
        if (key.startsWith("goal:")) {
            String val = text.substring(5).trim();
            String prefixTrans = langMap.get("goal:");
            if (prefixTrans == null) prefixTrans = "Goal:";
            return prefixTrans + " " + val;
        }

        // Handle dynamic patterns like "Hello, Guest"
        if (key.startsWith("hello,")) {
            String val = text.substring(6).trim();
            String prefixTrans = langMap.get("hello,");
            if (prefixTrans == null) prefixTrans = "Hello,";
            return prefixTrans + " " + val;
        }

        // Handle dynamic patterns like "Workout: "
        if (key.startsWith("🏋️ workout:")) {
            String val = text.substring(11).trim();
            String prefixTrans = langMap.get("workout: loading plan...");
            if (prefixTrans != null) {
                // If it matches workout loading
                if (key.contains("loading plan")) {
                    return prefixTrans;
                }
                // Otherwise replace only prefix
                return "🏋️ " + (cleanedLang.equals("ur") ? "ورزش: " : cleanedLang.equals("es") ? "Entrenamiento: " : "Training: ") + val;
            }
        }

        // Handle dynamic patterns like "Diet: "
        if (key.startsWith("🥗 diet:")) {
            String val = text.substring(8).trim();
            String prefixTrans = langMap.get("diet: loading plan...");
            if (prefixTrans != null) {
                if (key.contains("loading plan")) {
                    return prefixTrans;
                }
                return "🥗 " + (cleanedLang.equals("ur") ? "ڈائیٹ: " : cleanedLang.equals("es") ? "Dieta: " : "Diät: ") + val;
            }
        }

        String translated = langMap.get(key);
        return translated != null ? translated : text;
    }
}

package com.psm.medreminder;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class LocalStore {
    private static final String AUTH_PREF = "Mypref";
    private static final String DATA_PREF = "OfflineData";
    private static final String USERS = "users";
    private static final String MEDICINES = "medicines";

    private LocalStore() {
    }

    public static boolean login(Context context, String email, String password) {
        JSONObject user = findUser(context, email);
        if (user == null || !password.equals(user.optString("password"))) {
            return false;
        }
        writeSession(context, user);
        return true;
    }

    public static boolean userExists(Context context, String email) {
        return findUser(context, email) != null;
    }

    public static void saveUserAndLogin(Context context, String name, String gender, String birthdate,
                                        String email, String password, String weight, String height,
                                        String bloodpressure, String disease) {
        JSONArray users = getArray(context, USERS);
        JSONObject existing = findUser(context, email);
        String id = existing == null ? String.valueOf(System.currentTimeMillis()) : existing.optString("id");

        JSONObject user = buildUser(id, name, gender, birthdate, email, password, weight, height, bloodpressure, disease);
        removeUser(users, email);
        users.put(user);
        saveArray(context, USERS, users);
        writeSession(context, user);
    }

    public static void updateCurrentUser(Context context, String id, String name, String gender, String birthdate,
                                         String email, String weight, String height, String bloodpressure,
                                         String disease) {
        SharedPreferences session = context.getSharedPreferences(AUTH_PREF, Context.MODE_PRIVATE);
        String password = session.getString("password", "");
        JSONObject user = buildUser(id, name, gender, birthdate, email, password, weight, height, bloodpressure, disease);

        JSONArray users = getArray(context, USERS);
        removeUser(users, email);
        users.put(user);
        saveArray(context, USERS, users);
        writeSession(context, user);
    }

    public static void addMedicine(Context context, String userId, String name, String dosage, String schedule,
                                   String indication, String startDate, String endDate) {
        JSONArray medicines = getArray(context, MEDICINES);
        JSONObject medicine = new JSONObject();
        try {
            medicine.put("m_id", String.valueOf(System.currentTimeMillis()));
            medicine.put("user_id", userId);
            medicine.put("scientific_name", name);
            medicine.put("dosage", dosage);
            medicine.put("schedule", schedule);
            medicine.put("indication", indication);
            medicine.put("s_date", startDate);
            medicine.put("e_date", endDate);
            medicine.put("statuses", new JSONObject());
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
        medicines.put(medicine);
        saveArray(context, MEDICINES, medicines);
    }

    public static JSONArray getMedicines(Context context, String userId) {
        JSONArray result = new JSONArray();
        JSONArray medicines = getArray(context, MEDICINES);
        for (int i = 0; i < medicines.length(); i++) {
            JSONObject medicine = medicines.optJSONObject(i);
            if (medicine != null && userId.equals(medicine.optString("user_id"))) {
                result.put(medicine);
            }
        }
        return result;
    }

    public static JSONArray getMedicineStatuses(Context context, String userId, String date) {
        JSONArray result = new JSONArray();
        JSONArray medicines = getMedicines(context, userId);
        for (int i = 0; i < medicines.length(); i++) {
            JSONObject medicine = medicines.optJSONObject(i);
            if (medicine == null || !isDateInRange(date, medicine.optString("s_date"), medicine.optString("e_date"))) {
                continue;
            }
            JSONObject statusMap = medicine.optJSONObject("statuses");
            String status = statusMap == null ? "missed" : statusMap.optString(date, "missed");
            JSONObject row = new JSONObject();
            try {
                String mid = medicine.optString("m_id");
                row.put("id", mid);
                row.put("name", medicine.optString("scientific_name"));
                row.put("scientific_name", medicine.optString("scientific_name"));
                row.put("dosage", medicine.optString("dosage"));
                row.put("time", medicine.optString("schedule"));
                row.put("date", date);
                row.put("status", status);
                row.put("mid", mid);
                row.put("sid", mid + "-" + date);
                row.put("uid", userId);
                putAlarmParts(row, date, medicine.optString("schedule"));
                result.put(row);
            } catch (JSONException e) {
                throw new IllegalStateException(e);
            }
        }
        return result;
    }

    public static void markMedicineTaken(Context context, String medicineId, String userId, String date) {
        JSONArray medicines = getArray(context, MEDICINES);
        for (int i = 0; i < medicines.length(); i++) {
            JSONObject medicine = medicines.optJSONObject(i);
            if (medicine != null
                    && medicineId.equals(medicine.optString("m_id"))
                    && userId.equals(medicine.optString("user_id"))) {
                try {
                    JSONObject statuses = medicine.optJSONObject("statuses");
                    if (statuses == null) {
                        statuses = new JSONObject();
                        medicine.put("statuses", statuses);
                    }
                    statuses.put(date, "taken");
                    saveArray(context, MEDICINES, medicines);
                    return;
                } catch (JSONException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    private static JSONObject findUser(Context context, String email) {
        JSONArray users = getArray(context, USERS);
        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.optJSONObject(i);
            if (user != null && email.equalsIgnoreCase(user.optString("email"))) {
                return user;
            }
        }
        return null;
    }

    private static void removeUser(JSONArray users, String email) {
        for (int i = users.length() - 1; i >= 0; i--) {
            JSONObject user = users.optJSONObject(i);
            if (user != null && email.equalsIgnoreCase(user.optString("email"))) {
                users.remove(i);
            }
        }
    }

    private static JSONObject buildUser(String id, String name, String gender, String birthdate, String email,
                                        String password, String weight, String height, String bloodpressure,
                                        String disease) {
        JSONObject user = new JSONObject();
        try {
            user.put("id", id);
            user.put("name", name);
            user.put("gender", gender);
            user.put("birthdate", birthdate);
            user.put("email", email);
            user.put("password", password);
            user.put("weight", weight);
            user.put("height", height);
            user.put("bloodpressure", bloodpressure);
            user.put("disease", disease);
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
        return user;
    }

    private static void writeSession(Context context, JSONObject user) {
        SharedPreferences.Editor editor = context.getSharedPreferences(AUTH_PREF, Context.MODE_PRIVATE).edit();
        editor.putString("id", user.optString("id"));
        editor.putString("name", user.optString("name"));
        editor.putString("gender", user.optString("gender"));
        editor.putString("birthdate", user.optString("birthdate"));
        editor.putString("email", user.optString("email"));
        editor.putString("password", user.optString("password"));
        editor.putString("weight", user.optString("weight"));
        editor.putString("height", user.optString("height"));
        editor.putString("bloodpressure", user.optString("bloodpressure"));
        editor.putString("disease", user.optString("disease"));
        editor.apply();
    }

    private static JSONArray getArray(Context context, String key) {
        String raw = context.getSharedPreferences(DATA_PREF, Context.MODE_PRIVATE).getString(key, "[]");
        try {
            return new JSONArray(raw);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    private static void saveArray(Context context, String key, JSONArray array) {
        context.getSharedPreferences(DATA_PREF, Context.MODE_PRIVATE)
                .edit()
                .putString(key, array.toString())
                .apply();
    }

    private static boolean isDateInRange(String date, String start, String end) {
        Date selected = parseDate(date);
        Date startDate = parseDate(start);
        Date endDate = parseDate(end);
        if (selected == null || startDate == null || endDate == null) {
            return true;
        }
        return !selected.before(startDate) && !selected.after(endDate);
    }

    private static Date parseDate(String value) {
        String[] patterns = {"d-M-yyyy", "d/M/yyyy"};
        for (String pattern : patterns) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
                format.setLenient(false);
                return format.parse(value);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private static void putAlarmParts(JSONObject row, String date, String schedule) throws JSONException {
        String[] dateParts = date.split("-");
        String[] timeParts = schedule.split(":");
        row.put("year", dateParts.length == 3 ? parseInt(dateParts[2], 0) : 0);
        row.put("month", dateParts.length == 3 ? parseInt(dateParts[1], 1) : 1);
        row.put("day", dateParts.length == 3 ? parseInt(dateParts[0], 1) : 1);
        row.put("hour", timeParts.length >= 1 ? parseInt(timeParts[0], 9) : 9);
        row.put("minute", timeParts.length >= 2 ? parseInt(timeParts[1], 0) : 0);
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}

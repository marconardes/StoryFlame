import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

class ThemeController extends ChangeNotifier {
  ThemeController(this._prefs, this._isDark);

  final SharedPreferences _prefs;
  bool _isDark;

  bool get isDark => _isDark;

  static const _key = 'storyflame_theme_mode';

  static Future<ThemeController> load() async {
    final prefs = await SharedPreferences.getInstance();
    final isDark = prefs.getBool(_key) ?? false;
    return ThemeController(prefs, isDark);
  }

  void toggle() {
    _isDark = !_isDark;
    _prefs.setBool(_key, _isDark);
    notifyListeners();
  }
}

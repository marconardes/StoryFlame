import 'package:data/data.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'src/project_store.dart';
import 'src/theme_controller.dart';
import 'src/ui/project_details_page.dart';
import 'src/ui/project_list_page.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final repository = LocalProjectRepository();
  final store = ProjectStore(repository);
  await store.load();
  final themeController = await ThemeController.load();
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider<ProjectStore>.value(value: store),
        ChangeNotifierProvider<ThemeController>.value(value: themeController),
      ],
      child: const StoryFlameApp(),
    ),
  );
}

class StoryFlameApp extends StatelessWidget {
  const StoryFlameApp({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<ThemeController>(
      builder: (context, theme, _) {
        return MaterialApp(
          title: 'StoryFlame',
          debugShowCheckedModeBanner: false,
          themeMode: theme.isDark ? ThemeMode.dark : ThemeMode.light,
          theme: ThemeData(
            colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepOrange),
            useMaterial3: true,
          ),
          darkTheme: ThemeData(
            colorScheme: ColorScheme.fromSeed(
              seedColor: Colors.deepOrange,
              brightness: Brightness.dark,
            ),
            useMaterial3: true,
          ),
          routes: {'/': (_) => const ProjectListPage()},
          onGenerateRoute: (settings) {
            if (settings.name == ProjectDetailsPage.routeName) {
              final projectId = settings.arguments as String;
              return MaterialPageRoute(
                builder: (_) => ProjectDetailsPage(projectId: projectId),
              );
            }
            return null;
          },
        );
      },
    );
  }
}

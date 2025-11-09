import 'package:data/data.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

class FakeBundle extends CachingAssetBundle {
  FakeBundle(this.payload);

  final String payload;

  @override
  Future<ByteData> load(String key) {
    throw UnimplementedError('Binary load não necessário para estes testes.');
  }

  @override
  Future<String> loadString(String key, {bool cache = true}) async => payload;
}

void main() {
  test('MockProjectRepository parse projects from bundle', () async {
    const payload =
        '[{"id":"p","title":"Projeto","description":"","updatedAt":"2024-05-01T00:00:00Z","chapters":[]}]';
    final repository = MockProjectRepository(bundle: FakeBundle(payload));

    final projects = await repository.fetchProjects();

    expect(projects, hasLength(1));
    expect(projects.first.title, 'Projeto');
  });
}

# Agenten-Regeln

## Sprachvorgabe

**Alle Ausgaben, Antworten und Dokumentation sind auf Deutsch.**

- Antworte ausschliesslich auf Deutsch, sofern der Benutzer etwas anderes verlangt.
- Verwende deutsche Fachbegriffe; englische Begriffe ohne deutsche Entsprechung (z. B. API, Endpoint, Pipeline) bleiben auf Englisch.
- Variablennamen, Klassennamen und Funktionssignaturen folgen den Projektkonventionen (i. d. R. Englisch).

## Dokumentation

- READMEs, API-Dokumentationen und technische Dokumente werden auf Deutsch verfasst.
- Nutze klare Struktur mit Überschriften, Listen und Beispielen.
- Verwende konsistente Terminologie über alle Dokumente hinweg.

## Conventional Commits

**Alle Git-Commit-Nachrichten müssen dem Conventional Commits Standard folgen.**

### Format

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

### Erlaubte Typen

| Typ       | Beschreibung                                           |
|-----------|--------------------------------------------------------|
| `feat`    | Eine neue Funktion                                     |
| `fix`     | Eine Fehlerbehebung                                    |
| `docs`    | Nur Dokumentationsänderungen                           |
| `style`   | Änderungen, die den Code-Flow nicht beeinflussen (Formatierung, Leerzeichen, Semikolons) |
| `refactor`| Eine Code-Änderung, die weder einen Fehler behebt noch eine Funktion hinzufügt |
| `test`    | Addition or correction of tests                        |
| `chore`   | Changes to the build process or auxiliary tools        |

### Regeln

- Die `<description>` beginnt mit einem kleinen Buchstaben (im Deutschen üblich, z. B. "add feature" statt "Add feature")
- Kein Punkt am Ende der Beschreibung
- `<scope>` ist optional, aber empfohlen (z. B. `feat(api)`, `fix(parser)`)
- Commits ohne Conventional-Commit-Format werden nicht akzeptiert

### Beispiele

```
feat(auth): add login endpoint
fix(parser): handle malformed JSON input
docs(readme): update installation steps
refactor(database): simplify connection pooling
chore(deps): update dependencies
```

## Sonstiges

- Sei freundlich, präzise und professionell.
- Erkläre komplexe Konzepte verständlich.
- Kläre Unsicherheiten, bevor du Annahmen triffst.

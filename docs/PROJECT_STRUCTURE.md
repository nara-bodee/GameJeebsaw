# Project Structure

## Source Code
- `src/main` : Entry points and game window logic
- `src/core` : Core domain classes (player, settings)
- `src/story` : Story events and choices
- `src/shop` : Shop system UI
- `src/ui` : Main menu and front UI
- `src/save` : Current save/load system (5 slots)

## Assets
- `images` : UI and generic images
- `images_Story` : Story scene images by day

## Build / Runtime
- `bin` : Compiled `.class` output
- `saves` : Save slot files (`slot_1.sav` ... `slot_5.sav`)

## Legacy
- `legacy/Function_SaveGame` : Old save-system prototype and test files

## Documentation
- `docs` : Manuals and project notes

## Tools
- `run.bat` : Compile and run in one command (build to `bin`)
- `.vscode/tasks.json` : VS Code build/run tasks

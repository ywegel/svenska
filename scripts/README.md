# Python scripts to extract words

Use these scripts to extract words from swedish learning books. 

Currently supported:
- Rivstart A1/A2 second + third edition
- Rivstart B1/B2 second + third edition

Both scripts for these books require pymupdf:
```bash
pip install pymupdf
```

Run the scripts with python:
```bash
python3 rivstart_second_edition_wordlist_extractor.py
```

You can also use [uv](https://docs.astral.sh/uv/) to run the scripts without having to install the dependencies manually:
```bash
uv run rivstart_second_edition_wordlist_extractor.py
```

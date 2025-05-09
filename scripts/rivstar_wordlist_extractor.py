import fitz  # PyMuPDF
import json
import re

# Install dependencies:
# pip install pymupdf

# File paths
pdf_path = input("Your input pdf file name: ")
output_json = input("Your extracted words json output file name:")

def extract_words_from_pdf(pdf_path):
    data = []
    current_chapter = None
    chapter_words = []

    # Open the PDF
    pdf_document = fitz.open(pdf_path)

    for page in pdf_document:
        page_width = page.rect.width
        page_height = page.rect.height

        # Define two vertical column areas
        left_rect = fitz.Rect(0, 0, page_width / 2, page_height)
        right_rect = fitz.Rect(page_width / 2, 0, page_width, page_height)

        # Process both columns
        for column_rect in [left_rect, right_rect]:
            words_in_column = page.get_text('dict', clip=column_rect)['blocks']

            for block in words_in_column:
                if 'lines' in block:
                    for line in block['lines']:
                        swedish_word = ''
                        german_word = ''
                        for span in line['spans']:
                            if 'bold' in span['font'].lower():  # Bold words
                                german_word += span['text'] + ' '
                            else:
                                swedish_word += span['text'] + ' '

                        # Filter page numbers like "Sidan 123"
                        if re.search(r'\bSidan\s\d+\b', swedish_word) or re.search(r'\bSidan\s\d+\b', german_word):
                            continue

                        # Chapter indicators like "Kapitel 13"
                        if re.search(r'\bKapitel\s\d+\b', swedish_word) or re.search(r'\bKapitel\s\d+\b', german_word):
                            chapter_match = re.search(r'\bKapitel\s\d+\b', swedish_word) or re.search(r'\bKapitel\s\d+\b', german_word)
                            if chapter_match:
                                if current_chapter is not None:
                                    data.append({
                                        "chapter": current_chapter,
                                        "words": chapter_words
                                    })
                                    chapter_words = []
                                current_chapter = chapter_match.group()
                            continue

                        # Add words if both fields are filled
                        if swedish_word.strip() and german_word.strip():
                            chapter_words.append((swedish_word.strip(), german_word.strip()))

    # Add the last chapter
    if current_chapter is not None:
        data.append({
            "chapter": current_chapter,
            "words": chapter_words
        })

    pdf_document.close()
    return data

# Extract words and save as JSON
data = extract_words_from_pdf(pdf_path)
with open(output_json, 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=4)
print(f"The JSON file was successfully saved at: {output_json}")
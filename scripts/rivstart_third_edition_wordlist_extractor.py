import fitz  # PyMuPDF
import json
import re

# Use this for the third edition of the book. The book with the half yellow and half blue cover.

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
                        english_word = ''
                        for span in line['spans']:
                            if span['size'] == 11.0: # Skip "Sidan 123" page numbers
                                continue
                            elif span['size'] == 18.0: # Extract Chapters
                                chapter_match = re.search(r'\bKAPITEL\s\d+\b', span['text'])
                                if chapter_match:
                                    if current_chapter is not None:
                                        data.append({
                                            "chapter": current_chapter,
                                            "words": chapter_words
                                        })
                                        chapter_words = []
                                    current_chapter = chapter_match.group()
                                continue
                            elif span['size'] == 9.0:  # Swedish words
                                swedish_word += span['text'] + ' '
                            else:
                                english_word += span['text'] + ' ' # English words

                        # Add words if both fields are filled
                        if swedish_word.strip() and english_word.strip():
                            chapter_words.append((swedish_word.strip(), english_word.strip()))

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
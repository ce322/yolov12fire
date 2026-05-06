export function exportToExcelLikeCsv(filename, headers, rows) {
  const escapeCell = (value) => {
    if (value === null || value === undefined) return '';
    const text = String(value).replace(/"/g, '""');
    return /[",\n]/.test(text) ? `"${text}"` : text;
  };

  const csv = [
    headers.map((h) => escapeCell(h.label)).join(','),
    ...rows.map((row) => headers.map((h) => escapeCell(row[h.key])).join(','))
  ].join('\n');

  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = `${filename}.csv`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(link.href);
}

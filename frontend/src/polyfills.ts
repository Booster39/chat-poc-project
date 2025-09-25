import 'zone.js';
import process from 'process/browser';
import { Buffer } from 'buffer';

(window as any).global = window;
(window as any).process = process;
(window as any).Buffer = Buffer;